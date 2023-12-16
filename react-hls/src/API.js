export const authToken = "GENERATED TOKEN HERE";

// API call to create meeting
export const createMeeting = async ({ token }) => {
  const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
    method: "POST",
    headers: {
      authorization: `${authToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({}),
  });

  const { roomId } = await res.json();
  return roomId;
};

export const captureHLSThumbnail = async ({ roomId }) => {
  const res = await fetch(`https://api.videosdk.live/v2/hls/capture`, {
    method: "POST",
    headers: {
      authorization: `${authToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ roomId: roomId }),
  });

  const data = await res.json();
  return data;
};
