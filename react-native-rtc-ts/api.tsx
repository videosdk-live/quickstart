export const token: string = "ADD YOUR TOKEN HERE"; // token should be in String format

// API call to create meeting
export const createMeeting = async ({token}: {token: string}) => {
  const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
    method: 'POST',
    headers: {
      authorization: `${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({}),
  });
  const {roomId}:{roomId: string} = await res.json();
  console.log('room id', roomId);
  return roomId;
};
