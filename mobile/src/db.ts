import * as SQLite from 'expo-sqlite';
import type { Operation, Resource } from './types';

const db = SQLite.openDatabaseSync('cabbit.db');
export function initializeDb() {
  db.execSync(`PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;
    CREATE TABLE IF NOT EXISTS cached_records(id TEXT PRIMARY KEY,type TEXT NOT NULL,data TEXT NOT NULL,version INTEGER NOT NULL,updated_at TEXT NOT NULL,expires_at TEXT NOT NULL);
    CREATE INDEX IF NOT EXISTS ix_cache_type ON cached_records(type,updated_at);
    CREATE TABLE IF NOT EXISTS operations(operation_id TEXT PRIMARY KEY,entity_type TEXT NOT NULL,entity_id TEXT NOT NULL,action TEXT NOT NULL,version INTEGER,payload TEXT NOT NULL,local_timestamp TEXT NOT NULL,retry_count INTEGER NOT NULL DEFAULT 0,last_error TEXT);
    CREATE TABLE IF NOT EXISTS app_state(key TEXT PRIMARY KEY,value TEXT NOT NULL);`);
}
export function cacheRecords(records: Resource[]) {
  const expires = new Date(Date.now()+30*86400000).toISOString();
  db.withTransactionSync(()=>records.forEach(r=>db.runSync('INSERT OR REPLACE INTO cached_records(id,type,data,version,updated_at,expires_at) VALUES(?,?,?,?,?,?)',r.id,r.type,JSON.stringify(r.data),r.version,r.updatedAt,expires)));
}
export function cached(type: string): Resource[] {
  return db.getAllSync<{id:string;type:string;data:string;version:number;updated_at:string}>('SELECT * FROM cached_records WHERE type=? AND expires_at>? ORDER BY updated_at DESC',type,new Date().toISOString()).map(r=>({id:r.id,type:r.type,data:JSON.parse(r.data),version:r.version,createdAt:r.updated_at,updatedAt:r.updated_at}));
}
export function queue(op: Operation) { db.runSync('INSERT OR REPLACE INTO operations(operation_id,entity_type,entity_id,action,version,payload,local_timestamp) VALUES(?,?,?,?,?,?,?)',op.operationId,op.entityType,op.entityId,op.action,op.version??null,JSON.stringify(op.payload),op.localTimestamp); }
export function pending(): Operation[] { return db.getAllSync<any>('SELECT * FROM operations ORDER BY local_timestamp').map(r=>({operationId:r.operation_id,entityType:r.entity_type,entityId:r.entity_id,action:r.action,version:r.version??undefined,payload:JSON.parse(r.payload),localTimestamp:r.local_timestamp})); }
export function complete(ids:string[]){db.withTransactionSync(()=>ids.forEach(id=>db.runSync('DELETE FROM operations WHERE operation_id=?',id)));}
export function fail(id:string,error:string){db.runSync('UPDATE operations SET retry_count=retry_count+1,last_error=? WHERE operation_id=?',error.slice(0,500),id);}
export function replacePayload(id:string,payload:Record<string,unknown>){db.runSync('UPDATE operations SET payload=?,last_error=NULL WHERE operation_id=?',JSON.stringify(payload),id);}
export function state(key:string){return db.getFirstSync<{value:string}>('SELECT value FROM app_state WHERE key=?',key)?.value;}
export function setState(key:string,value:string){db.runSync('INSERT OR REPLACE INTO app_state(key,value) VALUES(?,?)',key,value);}
export function clearUserData(){db.execSync('DELETE FROM cached_records; DELETE FROM operations; DELETE FROM app_state;');}
