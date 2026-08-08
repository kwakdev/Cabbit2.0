import React, { useEffect } from 'react';
import { Stack, useRouter, useSegments } from 'expo-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { initializeDb } from '@/db';
import { useAuth } from '@/store';
import { watchConnectivity } from '@/sync';
import { theme } from '@/theme';
const queryClient=new QueryClient({defaultOptions:{queries:{retry:2,staleTime:30000}}});
export default function RootLayout(){const {session,ready,hydrate}=useAuth();const segments=useSegments();const router=useRouter();useEffect(()=>{initializeDb();hydrate();const stop=watchConnectivity();return stop;},[hydrate]);useEffect(()=>{if(!ready)return;const first=segments[0];if(!session&&first!=='login')router.replace('/login');else if(session?.user.mustChangePassword&&first!=='change-password')router.replace('/change-password');else if(session&&!session.user.mustChangePassword&&(first==='login'||first==='change-password'))router.replace('/(tabs)/today');},[ready,session,segments,router]);if(!ready)return null;return <QueryClientProvider client={queryClient}><Stack screenOptions={{headerStyle:{backgroundColor:theme.colors.cream},headerTintColor:theme.colors.forest,headerShadowVisible:false}}><Stack.Screen name="login" options={{headerShown:false}}/><Stack.Screen name="change-password" options={{title:'Secure your account',headerBackVisible:false}}/><Stack.Screen name="(tabs)" options={{headerShown:false}}/><Stack.Screen name="job/[id]" options={{title:'Work order'}}/><Stack.Screen name="resource/[type]" options={{title:'Operations'}}/><Stack.Screen name="dashboard" options={{title:'Dashboard'}}/><Stack.Screen name="employees" options={{title:'Employees'}}/></Stack></QueryClientProvider>}
