// Generate your token at https://app.videosdk.live/api-keys and paste it below.
export const authToken = "";

// API call to create stream
export const createStream = async ({ token }) => {
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
      throw new Error(`Failed to create stream: ${res.status}`);
    }

    //Destructuring the streamId from the response
    const { roomId: streamId } = await res.json();
    return streamId;
  } catch (error) {
    console.error("createStream failed", error);
    throw error;
  }
};
