import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/api';
import { Card, ScreenState } from '@/components';
import { theme } from '@/theme';
const labels:Record<string,string>={jobsToday:'Jobs today',jobsCompleted:'Completed',jobsOverdue:'Overdue',activeIncidents:'Active incidents',pendingTimeApprovals:'Time approvals'};
export default function Dashboard(){const query=useQuery({queryKey:['dashboard'],queryFn:api.dashboard});return <ScrollView style={s.screen} contentContainerStyle={s.content}><Text style={s.eyebrow}>MANAGER VIEW</Text><Text style={s.title}>Dashboard</Text>{query.isLoading?<ScreenState loading/>:query.error?<ScreenState error={query.error.message}/>:<View style={s.grid}>{Object.entries(query.data??{}).map(([key,value])=><Card key={key}><Text style={s.number}>{value}</Text><Text style={s.label}>{labels[key]??key}</Text></Card>)}</View>}</ScrollView>}
const s=StyleSheet.create({screen:{flex:1,backgroundColor:theme.colors.cream},content:{padding:20,gap:10},eyebrow:{fontSize:11,letterSpacing:1.8,fontWeight:'800',color:theme.colors.leaf},title:{fontSize:32,fontWeight:'900',color:theme.colors.ink},grid:{gap:10},number:{fontSize:32,fontWeight:'900',color:theme.colors.forest},label:{fontWeight:'700',color:theme.colors.muted}});
