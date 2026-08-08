import { create } from 'zustand';
import type { Session } from './types';
import { clearSession, loadSession, saveSession } from './session';
type AuthState={session:Session|null;ready:boolean;hydrate:()=>Promise<void>;setSession:(s:Session)=>Promise<void>;logout:()=>Promise<void>};
export const useAuth=create<AuthState>((set)=>({session:null,ready:false,hydrate:async()=>set({session:await loadSession(),ready:true}),setSession:async(session)=>{await saveSession(session);set({session});},logout:async()=>{await clearSession();set({session:null});}}));

