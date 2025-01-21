//Auth token we will use to generate a streamId and connect to it
export const authToken = "AUTH_TOKEN"; // You can get the token from the VideoSDK dashboard(app.videosdk.live)

// API call to create stream
export const createStream = async ({ token }) => {
  const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
    method: "POST",
    headers: {
      authorization: `${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({}),
  });
  //Destructuring the streamId from the response
  const { roomId: streamId } = await res.json();
  return streamId;
};
