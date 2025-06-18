/**
 * @format
 */

import { AppRegistry } from 'react-native';
// eslint-disable-next-line quotes
import App from "./App";
import { name as appName } from './app.json';
import { register } from '@videosdk.live/react-native-sdk';

register();
AppRegistry.registerComponent(appName, () => App);