// Generate two tokens at https://app.videosdk.live/api-keys and paste them below.
// hostToken → permission: allow_join. guestToken → permission: ask_join.
export const hostToken = "";
export const guestToken = "";

// API call to create meeting
export const createMeeting = async ({ token }) => {
  try {
    const res = await fetch(`https://api.videosdk.live/v2/rooms`, {
      method: "POST",
      headers: {
        authorization: token,
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
