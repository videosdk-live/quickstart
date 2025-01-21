export const authToken = "AUTH_TOKEN"; // You can get the token from the VideoSDK dashboard(app.videosdk.live)

// API call to create meeting
export const createStream = async ({ token }) => {
  const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
    method: "POST",
    headers: {
      authorization: `${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({}),
  });

  const { roomId } = await res.json();
  return roomId;
};
