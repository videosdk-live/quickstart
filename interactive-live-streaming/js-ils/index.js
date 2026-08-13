// getting Elements from Dom
const elements = {
  createLiveStreamBtn: document.getElementById("createLiveStreamBtn"),
  joinHostButton: document.getElementById("joinHostBtn"),
  joinAudienceButton: document.getElementById("joinAudienceBtn"),
  leaveButton: document.getElementById("leaveBtn"),
  toggleMicButton: document.getElementById("toggleMicBtn"),
  toggleWebCamButton: document.getElementById("toggleWebCamBtn"),
  videoContainer: document.getElementById("videoContainer"),
  textDiv: document.getElementById("textDiv"),
  switchModeBtn: document.getElementById("switchModeBtn"),
};

// declare Variables
let liveStream = null;
let streamId = "";
let isMicOn = false;
let isWebCamOn = false;

const Constants = VideoSDK.Constants;

// Initialize live stream
async function initializeLiveStream(mode) {
  try {
    await window.VideoSDK.config(TOKEN);
    liveStream = await window.VideoSDK.initMeeting({
      meetingId: streamId,
      name: "Thomas Edison",
      webcamEnabled: true,
      micEnabled: true,
      mode: mode,
    });

    await liveStream.join();
    setupLiveStreamEventHandlers(mode);
  } catch (error) {
    console.error("Failed to initialize live stream", error);
    elements.textDiv.textContent =
      "Unable to join the live stream. Please try again.";
  }
}

// Function to update controls visibility
function updateControlsVisibility(mode) {
  const isHost = mode === Constants.modes.SEND_AND_RECV;
  elements.toggleMicButton.style.display = isHost ? "inline-block" : "none";
  elements.toggleWebCamButton.style.display = isHost ? "inline-block" : "none";
  elements.switchModeBtn.textContent = isHost
    ? "Switch to Audience Mode"
    : "Switch to Host Mode";
}

function setupLiveStreamEventHandlers(mode) {
  liveStream.on("meeting-joined", () => handleLiveStreamJoined(mode));
  liveStream.on("meeting-left", () => (elements.videoContainer.innerHTML = ""));
  liveStream.on("participant-joined", handleParticipantJoined);
  liveStream.on("participant-left", handleParticipantLeft);
  liveStream.on("participant-mode-changed", handleParticipantModeChange);
}

function handleLiveStreamJoined(mode) {
  elements.textDiv.textContent = null;
  document.getElementById("grid-screen").style.display = "block";
  document.getElementById("heading").textContent = `Stream Id: ${streamId}`;
  updateControlsVisibility(mode);
  if (mode === Constants.modes.SEND_AND_RECV) {
    createLocalParticipant();
    liveStream.localParticipant.on("stream-enabled", (stream) =>
      setTrack(stream, null, liveStream.localParticipant, true)
    );
  }
}

function handleParticipantModeChange(data) {
  const { mode, participantId } = data;
  const isLocal = liveStream.localParticipant.id === participantId;
  if (isLocal) {
    updateControlsVisibility(mode);
    if (mode === Constants.modes.SEND_AND_RECV) {
      createLocalParticipant();
      liveStream.localParticipant.on("stream-enabled", (stream) =>
        setTrack(stream, null, liveStream.localParticipant, true)
      );
    } else {
      removeLocalVideo();
    }
  } else {
    if (mode === Constants.modes.SEND_AND_RECV) {
      const participant = liveStream.participants.get(participantId);
      if (participant) {
        // Ensure video and audio elements are re-created
        const videoElement = createVideoElement(
          participant.id,
          participant.displayName
        );
        const audioElement = createAudioElement(participant.id);

        participant.on("stream-enabled", (stream) =>
          setTrack(stream, audioElement, participant, false)
        );
        // Append to the container
        elements.videoContainer.append(videoElement, audioElement);
      }
    } else {
      removeParticipantMedia(participantId);
    }
  }
}
function handleParticipantJoined(participant) {
  if (participant.mode === Constants.modes.SEND_AND_RECV) {
    const videoElement = createVideoElement(
      participant.id,
      participant.displayName
    );
    const audioElement = createAudioElement(participant.id);
    participant.on("stream-enabled", (stream) =>
      setTrack(stream, audioElement, participant, false)
    );
    elements.videoContainer.append(videoElement, audioElement);
  }
}
function handleParticipantLeft(participant) {
  removeParticipantMedia(participant.id);
}
function removeLocalVideo() {
  const localVideo = document.getElementById(
    `f-${liveStream.localParticipant.id}`
  );
  if (localVideo) localVideo.remove();
}
function removeParticipantMedia(participantId) {
  document.getElementById(`f-${participantId}`)?.remove();
  document.getElementById(`a-${participantId}`)?.remove();
}

// creating video element
function createVideoElement(pId, name) {
  const videoFrame = document.createElement("div");
  videoFrame.id = `f-${pId}`;
  const videoElement = document.createElement("video");
  videoElement.classList.add("video-frame");
  videoElement.id = `v-${pId}`;
  videoElement.playsInline = true;
  videoElement.width = 300;
  videoFrame.append(videoElement, createDisplayNameElement(name));
  return videoFrame;
}

function createDisplayNameElement(name) {
  const displayName = document.createElement("div");
  displayName.innerHTML = `Name : ${name}`;
  return displayName;
}

// creating audio element
function createAudioElement(pId) {
  const audioElement = document.createElement("audio");
  audioElement.id = `a-${pId}`;
  audioElement.autoPlay = false;
  audioElement.playsInline = true;
  audioElement.controls = false;
  audioElement.style.display = "none";
  return audioElement;
}

// creating local participant
function createLocalParticipant() {
  const localParticipant = createVideoElement(
    liveStream.localParticipant.id,
    liveStream.localParticipant.displayName
  );
  elements.videoContainer.appendChild(localParticipant);
}

// setting media track
function setTrack(stream, audioElement, participant, isLocal) {
  const mediaStream = new MediaStream();
  mediaStream.addTrack(stream.track);
  if (stream.kind === "video") {
    isWebCamOn = true;
    const videoElm = document.getElementById(`v-${participant.id}`);
    videoElm.srcObject = mediaStream;
    videoElm
      .play()
      .catch((error) => console.error("videoElem.play() failed", error));
  } else if (stream.kind === "audio") {
    if (isLocal) isMicOn = true;
    audioElement.srcObject = mediaStream;
    audioElement
      .play()
      .catch((error) => console.error("audioElem.play() failed", error));
  }
}

// Create Live Stream Button Event Listener
elements.createLiveStreamBtn.addEventListener("click", async () => {
  await createLiveStream();
});

// Join Live Stream As Host Button Event Listener
elements.joinHostButton.addEventListener("click", async () => {
  await joinLiveStream(Constants.modes.SEND_AND_RECV);
});

// Join Live Stream As Viewer Button Event Listener
elements.joinAudienceButton.addEventListener("click", async () => {
  await joinLiveStream(Constants.modes.RECV_ONLY);
});

// leave Live Stream Button Event Listener
elements.leaveButton.addEventListener("click", async () => {
  try {
    await liveStream?.leave();
  } catch (error) {
    console.error("Failed to leave live stream", error);
  }
  document.getElementById("grid-screen").style.display = "none";
  document.getElementById("join-screen").style.display = "block";
});

// Toggle Mic Button Event Listener
elements.toggleMicButton.addEventListener("click", async () => {
  try {
    if (isMicOn) {
      await liveStream?.muteMic();
    } else {
      await liveStream?.unmuteMic();
    }
    isMicOn = !isMicOn;
  } catch (error) {
    console.error("Failed to toggle mic", error);
  }
});

// Toggle Web Cam Button Event Listener
elements.toggleWebCamButton.addEventListener("click", async () => {
  try {
    if (isWebCamOn) {
      await liveStream?.disableWebcam();
    } else {
      await liveStream?.enableWebcam();
    }
    const vElement = document.getElementById(
      `f-${liveStream.localParticipant.id}`
    );
    if (vElement) vElement.style.display = isWebCamOn ? "none" : "inline";
    isWebCamOn = !isWebCamOn;
  } catch (error) {
    console.error("Failed to toggle webcam", error);
  }
});

// Update switch mode button handler
elements.switchModeBtn.addEventListener("click", async () => {
  const newMode =
    liveStream.localParticipant.mode === Constants.modes.SEND_AND_RECV
      ? Constants.modes.RECV_ONLY
      : Constants.modes.SEND_AND_RECV;
  try {
    await liveStream.changeMode(newMode);
  } catch (error) {
    console.error("Failed to change mode", error);
  }
});

// Helper functions for live stream creation and joining
async function createLiveStream() {
  document.getElementById("join-screen").style.display = "none";
  elements.textDiv.textContent = "Creating new Live Stream...";
  try {
    const { roomId } = await fetchLiveStreamRoom();
    streamId = roomId;
    await initializeLiveStream(Constants.modes.SEND_AND_RECV);
  } catch (error) {
    console.error("Failed to create live stream", error);
    elements.textDiv.textContent =
      "Unable to create the live stream. Please try again.";
  }
}

async function joinLiveStream(mode) {
  const roomId = document.getElementById("streamIdText").value;
  if (!roomId) return alert("Please enter Stream Id");
  document.getElementById("join-screen").style.display = "none";
  elements.textDiv.textContent = `Joining the Live Stream as ${
    mode === Constants.modes.SEND_AND_RECV ? "host" : "audience"
  }...`;
  streamId = roomId;
  await initializeLiveStream(mode);
}

async function fetchLiveStreamRoom() {
  const url = `https://api.videosdk.live/v2/rooms`;
  const options = {
    method: "POST",
    headers: { Authorization: TOKEN, "Content-Type": "application/json" },
  };
  const response = await fetch(url, options);
  return response.json();
}
