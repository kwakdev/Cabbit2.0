import React, { useCallback, useEffect, useState } from 'react';
import { RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import NetInfo from '@react-native-community/netinfo';
import { api } from './api';
import { cached, cacheRecords } from './db';
import type { Resource } from './types';
import { Card, ScreenState, StatusPill } from './components';
import { theme } from './theme';

const label=(r:Resource)=>String(r.data.title??r.data.name??r.data.description??r.id);
export function ResourceList({type,title,detailRoute}:{type:string;title:string;detailRoute?:string}){
  const router=useRouter();const [items,setItems]=useState<Resource[]>(cached(type));const [loading,setLoading]=useState(items.length===0);const [error,setError]=useState('');
  const load=useCallback(async()=>{setError('');try{const net=await NetInfo.fetch();if(net.isConnected){const page=await api.list(type);cacheRecords(page.items);setItems(page.items);}else setItems(cached(type));}catch(e){setError(e instanceof Error?e.message:'Could not load');setItems(cached(type));}finally{setLoading(false);}},[type]);
  useEffect(()=>{load();},[load]);
  return <ScrollView style={s.screen} contentContainerStyle={s.content} refreshControl={<RefreshControl refreshing={loading} onRefresh={load}/>}><View><Text style={s.eyebrow}>FIELD OPERATIONS</Text><Text style={s.title}>{title}</Text></View>{loading&&items.length===0?<ScreenState loading/>:error&&items.length===0?<ScreenState error={error}/>:items.length===0?<ScreenState empty={`No ${title.toLowerCase()} are available.`}/>:items.map(r=><Card key={r.id} onPress={detailRoute?()=>router.push(`${detailRoute}/${r.id}` as never):undefined}><View style={s.row}><Text style={s.cardTitle} numberOfLines={2}>{label(r)}</Text>{Boolean(r.data.status)&&<StatusPill label={String(r.data.status).replaceAll('_',' ')}/>}</View>{Boolean(r.data.scheduledStart)&&<Text style={s.meta}>{new Date(String(r.data.scheduledStart)).toLocaleString()}</Text>}{Boolean(r.data.address)&&<Text style={s.meta}>{String(r.data.address)}</Text>}</Card>)}</ScrollView>;
}
const s=StyleSheet.create({screen:{flex:1,backgroundColor:theme.colors.cream},content:{padding:20,gap:12,paddingBottom:40},eyebrow:{fontSize:11,letterSpacing:1.8,fontWeight:'800',color:theme.colors.leaf},title:{fontSize:32,fontWeight:'900',color:theme.colors.ink,marginTop:4,marginBottom:8},row:{flexDirection:'row',justifyContent:'space-between',gap:12,alignItems:'flex-start'},cardTitle:{fontSize:17,fontWeight:'800',color:theme.colors.ink,flex:1},meta:{color:theme.colors.muted,fontSize:14}});
