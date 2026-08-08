import React from 'react';
import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { theme } from '@/theme';
const icons:Record<string,keyof typeof Ionicons.glyphMap>={today:'today-outline',jobs:'briefcase-outline',time:'stopwatch-outline',notifications:'notifications-outline',more:'grid-outline'};
export default function TabsLayout(){return <Tabs screenOptions={({route})=>({headerShown:false,tabBarActiveTintColor:theme.colors.forest,tabBarInactiveTintColor:theme.colors.muted,tabBarStyle:{height:64,paddingBottom:8,paddingTop:6,backgroundColor:'#fff',borderTopColor:theme.colors.line},tabBarIcon:({color,size})=><Ionicons name={icons[route.name]??'ellipse-outline'} color={color} size={size}/>})}><Tabs.Screen name="today" options={{title:'Today'}}/><Tabs.Screen name="jobs" options={{title:'Jobs'}}/><Tabs.Screen name="time" options={{title:'Time'}}/><Tabs.Screen name="notifications" options={{title:'Alerts'}}/><Tabs.Screen name="more" options={{title:'More'}}/></Tabs>}

