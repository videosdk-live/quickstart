// getting Elements from Dom
const joinButton = document.getElementById("joinBtn");
const leaveButton = document.getElementById("leaveBtn");
const toggleMicButton = document.getElementById("toggleMicBtn");
const toggleWebCamButton = document.getElementById("toggleWebCamBtn");
const createButton = document.getElementById("createMeetingBtn");
const videoContainer = document.getElementById("videoContainer");
const textDiv = document.getElementById("textDiv");

// declare Variables
let meeting = null;
let meetingId = "";
let isMicOn = false;
let isWebCamOn = false;

function showJoinScreen(message) {
  document.getElementById("join-screen").style.display = "block";
  document.getElementById("grid-screen").style.display = "none";
  textDiv.textContent = message ?? "";
}

// Join Meeting Button Event Listener
joinButton.addEventListener("click", async () => {
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  await initializeMeeting();
});

// Create Meeting Button Event Listener
createButton.addEventListener("click", async () => {
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Please wait, we are joining the meeting";

  const url = `https://api.videosdk.live/v2/rooms`;
  const options = {
    method: "POST",
    headers: { Authorization: TOKEN, "Content-Type": "application/json" },
  };

  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`Failed to create meeting: ${response.status}`);
    }
    const { roomId } = await response.json();
    meetingId = roomId;
    await initializeMeeting();
  } catch (error) {
    console.error("Failed to create meeting", error);
    showJoinScreen(
      "Unable to create the meeting. Check your token and try again."
    );
  }
});

// Initialize meeting
async function initializeMeeting() {
  try {
    // VideoSDK.config and initMeeting are synchronous in 1.x — no await.
    window.VideoSDK.config(TOKEN);

    meeting = window.VideoSDK.initMeeting({
      meetingId: meetingId, // required
      name: "C.V.Raman", // required
      micEnabled: true, // optional, default: true
      webcamEnabled: true, // optional, default: true
    });

    await meeting.join();
  } catch (error) {
    console.error("Failed to initialize meeting", error);
    showJoinScreen("Unable to join the meeting. Please try again.");
    return;
  }

  // creating local participant
  createLocalParticipant();

  // setting local participant stream
  meeting.localParticipant.on("stream-enabled", (stream) => {
    setTrack(stream, null, meeting.localParticipant, true);
  });

  meeting.on("meeting-joined", () => {
    textDiv.textContent = null;

    document.getElementById("grid-screen").style.display = "block";
    document.getElementById(
      "meetingIdHeading"
    ).textContent = `Meeting Id: ${meetingId}`;
  });

  meeting.on("meeting-left", () => {
    videoContainer.innerHTML = "";
  });

  //  participant joined
  meeting.on("participant-joined", (participant) => {
    let videoElement = createVideoElement(
      participant.id,
      participant.displayName
    );
    let audioElement = createAudioElement(participant.id);

    participant.on("stream-enabled", (stream) => {
      setTrack(stream, audioElement, participant, false);
    });
    videoContainer.appendChild(videoElement);
    videoContainer.appendChild(audioElement);
  });

  // participants left
  meeting.on("participant-left", (participant) => {
    let vElement = document.getElementById(`f-${participant.id}`);
    vElement.remove(vElement);

    let aElement = document.getElementById(`a-${participant.id}`);
    aElement.remove(aElement);
  });
}

// creating video element
function createVideoElement(pId, name) {
  let videoFrame = document.createElement("div");
  videoFrame.setAttribute("id", `f-${pId}`);

  //create video
  let videoElement = document.createElement("video");
  videoElement.classList.add("video-frame");
  videoElement.setAttribute("id", `v-${pId}`);
  videoElement.setAttribute("playsinline", true);
  videoElement.setAttribute("width", "300");
  videoFrame.appendChild(videoElement);

  let displayName = document.createElement("div");
  displayName.innerHTML = `Name : ${name}`;

  videoFrame.appendChild(displayName);
  return videoFrame;
}

// creating audio element
function createAudioElement(pId) {
  let audioElement = document.createElement("audio");
  audioElement.setAttribute("autoPlay", "false");
  audioElement.setAttribute("playsInline", "true");
  audioElement.setAttribute("controls", "false");
  audioElement.setAttribute("id", `a-${pId}`);
  audioElement.style.display = "none";
  return audioElement;
}

// creating local participant
function createLocalParticipant() {
  let localParticipant = createVideoElement(
    meeting.localParticipant.id,
    meeting.localParticipant.displayName
  );
  videoContainer.appendChild(localParticipant);
}

// setting media track
function setTrack(stream, audioElement, participant, isLocal) {
  if (stream.kind == "video") {
    isWebCamOn = true;
    const mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    let videoElm = document.getElementById(`v-${participant.id}`);
    videoElm.srcObject = mediaStream;
    videoElm
      .play()
      .catch((error) =>
        console.error("videoElem.current.play() failed", error)
      );
  }
  if (stream.kind == "audio") {
    if (isLocal) {
      isMicOn = true;
    } else {
      const mediaStream = new MediaStream();
      mediaStream.addTrack(stream.track);
      audioElement.srcObject = mediaStream;
      audioElement
        .play()
        .catch((error) => console.error("audioElem.play() failed", error));
    }
  }
}

// leave Meeting Button Event Listener
leaveButton.addEventListener("click", async () => {
  try {
    await meeting?.leave();
  } catch (error) {
    console.error("Failed to leave meeting", error);
  }
  document.getElementById("grid-screen").style.display = "none";
  document.getElementById("join-screen").style.display = "block";
});

// Toggle Mic Button Event Listener
toggleMicButton.addEventListener("click", async () => {
  try {
    if (isMicOn) {
      // Disable Mic in Meeting
      await meeting?.muteMic();
    } else {
      // Enable Mic in Meeting
      await meeting?.unmuteMic();
    }
    isMicOn = !isMicOn;
  } catch (error) {
    console.error("Failed to toggle mic", error);
  }
});

// Toggle Web Cam Button Event Listener
toggleWebCamButton.addEventListener("click", async () => {
  // Only the SDK call is wrapped in try/catch. DOM work runs after a successful
  // toggle so a missing element can't hide the fact that isWebCamOn is stale.
  // Not using `meeting?.` — if meeting is null we want the SDK call to throw
  // into the catch, not fall through to the un-guarded DOM access below.
  try {
    if (isWebCamOn) {
      await meeting.disableWebcam();
    } else {
      await meeting.enableWebcam();
    }
  } catch (error) {
    console.error("Failed to toggle webcam", error);
    return;
  }

  isWebCamOn = !isWebCamOn;
  const vElement = document.getElementById(`f-${meeting.localParticipant.id}`);
  if (vElement) {
    vElement.style.display = isWebCamOn ? "inline" : "none";
  }
});
