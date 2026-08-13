// Learn more https://docs.expo.io/guides/customizing-metro
const { getDefaultConfig } = require("expo/metro-config");
const resolveFrom = require("resolve-from");

/** @type {import('expo/metro-config').MetroConfig} */
const config = getDefaultConfig(__dirname);

// Fix for @videosdk.live/react-native-webrtc requiring event-target-shim v6
// while React Native requires v5.
config.resolver.resolveRequest = (context, moduleName, platform) => {
  if (
    moduleName.startsWith("event-target-shim") &&
    context.originModulePath.includes("@videosdk.live/react-native-webrtc")
  ) {
    const updatedModuleName = moduleName.endsWith("/index")
      ? moduleName.replace("/index", "")
      : moduleName;
    const eventTargetShimPath = resolveFrom(
      context.originModulePath,
      updatedModuleName
    );

    return {
      filePath: eventTargetShimPath,
      type: "sourceFile",
    };
  }

  return context.resolveRequest(context, moduleName, platform);
};

module.exports = config;
