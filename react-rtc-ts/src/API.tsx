export const authToken: string = "YOUR GENERATED TOKEN HERE";

// API call to create meeting
export const createMeeting = async ({ token }: {token: string}) => {
  const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
    method: "POST",
    headers: {
      authorization: `${authToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({}),
  });

  const { roomId }: {roomId: string} = await res.json();
  return roomId;
};
