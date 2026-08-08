import React, { useMemo, useState } from 'react';
import { Alert, Image, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Stack, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import * as Crypto from 'expo-crypto';
import * as ImagePicker from 'expo-image-picker';
import * as ImageManipulator from 'expo-image-manipulator';
import { Button, Card, Field, StatusPill } from '@/components';
import { cached, queue } from '@/db';
import { synchronize } from '@/sync';
import { theme } from '@/theme';

type ChecklistItem={id:string;label:string;required?:boolean};
export default function JobDetail(){
  const {id}=useLocalSearchParams<{id:string}>();
  const job=useMemo(()=>cached('work-orders').find(x=>x.id===id),[id]);
  const items=(job?.data.checklistItems as ChecklistItem[]|undefined)??[];
  const [answers,setAnswers]=useState<Record<string,boolean>>({});
  const [notes,setNotes]=useState(String(job?.data.completionNotes??''));
  const [photos,setPhotos]=useState<string[]>([]);
  if(!job)return <View style={s.center}><Text>Work order is not available offline.</Text></View>;
  const current=job;
  const status=String(current.data.status??'ASSIGNED');
  const missing=items.filter(item=>item.required&&!answers[item.id]);
  function mutate(data:Record<string,unknown>){queue({operationId:Crypto.randomUUID(),entityType:'work-orders',entityId:current.id,action:'UPDATE',version:current.version,localTimestamp:new Date().toISOString(),payload:{...current.data,...data}});synchronize().catch(()=>undefined);Alert.alert('Saved','This change is stored safely and will sync when connected.');}
  function toggle(item:ChecklistItem){const next={...answers,[item.id]:!answers[item.id]};setAnswers(next);queue({operationId:Crypto.randomUUID(),entityType:'checklists',entityId:Crypto.randomUUID(),action:'CREATE',localTimestamp:new Date().toISOString(),payload:{workOrderId:current.id,itemId:item.id,label:item.label,value:next[item.id],required:Boolean(item.required)}});}
  function complete(){if(missing.length){Alert.alert('Checklist incomplete',`${missing.length} required item${missing.length===1?' is':'s are'} still open.`);return;}if(Array.isArray(current.data.requiredPhotoTypes)&&current.data.requiredPhotoTypes.length>0&&photos.length===0){Alert.alert('Photo required','Capture the required job photo before completing this work order.');return;}mutate({status:'COMPLETED',completionNotes:notes,completedAt:new Date().toISOString(),checklistComplete:true});}
  async function photo(){const permission=await ImagePicker.requestCameraPermissionsAsync();if(!permission.granted){Alert.alert('Camera permission needed');return;}const result=await ImagePicker.launchCameraAsync({quality:.85,exif:false});if(result.canceled)return;const original=result.assets[0];if(!original)return;const asset=await ImageManipulator.manipulateAsync(original.uri,[{resize:{width:1920}}],{compress:.78,format:ImageManipulator.SaveFormat.JPEG});setPhotos(p=>[...p,asset.uri]);queue({operationId:Crypto.randomUUID(),entityType:'photos',entityId:Crypto.randomUUID(),action:'CREATE',localTimestamp:new Date().toISOString(),payload:{workOrderId:current.id,kind:'PROGRESS',localUri:asset.uri,mimeType:'image/jpeg',capturedAt:new Date().toISOString()}});}
  function incident(){queue({operationId:Crypto.randomUUID(),entityType:'incidents',entityId:Crypto.randomUUID(),action:'CREATE',localTimestamp:new Date().toISOString(),payload:{workOrderId:current.id,severity:'HIGH',description:'Field incident - details required',status:'OPEN'}});Alert.alert('Draft saved','Open Incidents from More to add details.');}
  return <ScrollView style={s.screen} contentContainerStyle={s.content}>
    <Stack.Screen options={{title:String(current.data.workOrderNumber??'Work order')}}/>
    <View><Text style={s.eyebrow}>{String(current.data.workOrderNumber??'WORK ORDER')}</Text><Text style={s.title}>{String(current.data.title??'Untitled job')}</Text><StatusPill label={status} tone={status==='COMPLETED'?'good':status==='CANCELLED'?'bad':'warn'}/></View>
    <Card><Text style={s.heading}>Site</Text><Text style={s.body}>{String(current.data.address??current.data.locationName??'No address recorded')}</Text><Text style={s.body}>{String(current.data.instructions??'No special instructions')}</Text></Card>
    <Card><Text style={s.heading}>Field actions</Text><View style={s.actions}><Button label={status==='IN_PROGRESS'?'Pause':'Start work'} onPress={()=>mutate({status:status==='IN_PROGRESS'?'PAUSED':'IN_PROGRESS'})}/><Button label="Capture photo" onPress={photo} variant="secondary"/></View>{photos.map(uri=><Image key={uri} source={{uri}} style={s.photo}/>)}</Card>
    <Card><View style={s.row}><Text style={s.heading}>Checklist</Text><StatusPill label={`${items.length-missing.length}/${items.length}`} tone={missing.length?'warn':'good'}/></View>{items.length?items.map(item=><Pressable accessibilityRole="checkbox" accessibilityState={{checked:Boolean(answers[item.id])}} key={item.id} onPress={()=>toggle(item)} style={s.check}><Ionicons name={answers[item.id]?'checkbox':'square-outline'} size={25} color={answers[item.id]?theme.colors.leaf:theme.colors.muted}/><Text style={s.checkText}>{item.label}{item.required?' *':''}</Text></Pressable>):<Text style={s.body}>No checklist is attached.</Text>}</Card>
    <Card><Text style={s.heading}>Completion</Text><Field label="Completion notes" value={notes} onChangeText={setNotes} multiline/><Button label="Complete job" onPress={complete}/></Card>
    <Card><Text style={s.heading}>Safety and problems</Text><Text style={s.body}>Critical reports alert a manager. Photos are retained with the incident.</Text><Button label="Report an incident" onPress={incident} variant="danger"/></Card>
  </ScrollView>;
}
const s=StyleSheet.create({screen:{flex:1,backgroundColor:theme.colors.cream},content:{padding:20,gap:14,paddingBottom:50},center:{flex:1,alignItems:'center',justifyContent:'center',padding:30},eyebrow:{fontSize:11,letterSpacing:1.7,fontWeight:'800',color:theme.colors.leaf},title:{fontSize:30,fontWeight:'900',color:theme.colors.ink,marginVertical:6},heading:{fontSize:17,fontWeight:'900',color:theme.colors.ink},body:{fontSize:15,lineHeight:22,color:theme.colors.muted},actions:{gap:10},photo:{width:'100%',height:210,borderRadius:12,backgroundColor:theme.colors.line},row:{flexDirection:'row',justifyContent:'space-between',alignItems:'center'},check:{flexDirection:'row',alignItems:'center',gap:10,paddingVertical:8},checkText:{fontSize:15,color:theme.colors.ink,flex:1}});
