// Generate your token at https://app.videosdk.live/api-keys and paste it below.
export const authToken = "";

// API call to create meeting
export const createMeeting = async ({ token }) => {
  try {
    const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
      method: "POST",
      headers: {
        authorization: `${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({}),
    });

    if (!res.ok) {
      throw new Error(`Failed to create meeting: ${res.status}`);
    }

    const { roomId } = await res.json();
    return roomId;
  } catch (error) {
    console.error("createMeeting failed", error);
    throw error;
  }
};

export const captureHLSThumbnail = async ({ roomId, token }) => {
  try {
    const res = await fetch(`https://api.videosdk.live/v2/hls/capture`, {
      method: "POST",
      headers: {
        authorization: `${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ roomId: roomId }),
    });

    if (!res.ok) {
      throw new Error(`Failed to capture HLS thumbnail: ${res.status}`);
    }

    return await res.json();
  } catch (error) {
    console.error("captureHLSThumbnail failed", error);
    throw error;
  }
};
