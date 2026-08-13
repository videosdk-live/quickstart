// Generate your token at https://app.videosdk.live/api-keys and paste it below.
export const token: string = '';

// API call to create meeting
export const createMeeting = async ({token}: {token: string}) => {
  try {
    const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
      method: 'POST',
      headers: {
        authorization: `${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });

    if (!res.ok) {
      throw new Error(`Failed to create meeting: ${res.status}`);
    }

    const {roomId}: {roomId: string} = await res.json();
    return roomId;
  } catch (error) {
    console.error('createMeeting failed', error);
    throw error;
  }
};
