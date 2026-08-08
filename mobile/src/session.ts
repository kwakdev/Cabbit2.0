import * as SecureStore from 'expo-secure-store';
import type { Session } from './types';
const KEY='cabbit.session.v1';
export async function loadSession(){const value=await SecureStore.getItemAsync(KEY,{requireAuthentication:false});return value?JSON.parse(value) as Session:null;}
export async function saveSession(session:Session){await SecureStore.setItemAsync(KEY,JSON.stringify(session),{keychainAccessible:SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY});}
export async function clearSession(){await SecureStore.deleteItemAsync(KEY);}

