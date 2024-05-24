// getting Elements from Dom
const joinHostButton = document.getElementById("joinHostBtn");
const joinViewerButton = document.getElementById("joinViewerBtn");
const leaveButton = document.getElementById("leaveBtn");
const startHlsButton = document.getElementById("startHlsBtn");
const stopHlsButton = document.getElementById("stopHlsBtn");
const toggleMicButton = document.getElementById("toggleMicBtn");
const toggleWebCamButton = document.getElementById("toggleWebCamBtn");
const createButton = document.getElementById("createMeetingBtn");
const videoContainer = document.getElementById("videoContainer");
const textDiv = document.getElementById("textDiv");
const hlsStatusHeading = document.getElementById("hlsStatusHeading");

// declare Variables
let meeting = null;
let meetingId = "";
let isMicOn = false;
let isWebCamOn = false;

const Constants = VideoSDK.Constants;

// Initialize meeting
function initializeMeeting(mode) {
  window.VideoSDK.config(TOKEN);

  meeting = window.VideoSDK.initMeeting({
    meetingId: meetingId, // required
    name: "Thomas Edison", // required
    mode: mode,
  });

  meeting.join();

  meeting.on("meeting-joined", () => {
    textDiv.textContent = null;

    document.getElementById("grid-screen").style.display = "block";
    document.getElementById(
      "meetingIdHeading"
    ).textContent = `Meeting Id: ${meetingId}`;

    if (meeting.hlsState === Constants.hlsEvents.HLS_STOPPED) {
      hlsStatusHeading.textContent = "HLS has not stared yet";
    } else {
      hlsStatusHeading.textContent = `HLS Status: ${meeting.hlsState}`;
    }

    if (mode === Constants.modes.CONFERENCE) {
      document.getElementById("speakerView").style.display = "block";
    }
  });

  meeting.on("meeting-left", () => {
    videoContainer.innerHTML = "";
  });

  meeting.on("hls-state-changed", (data) => {
    const { status } = data;

    hlsStatusHeading.textContent = `HLS Status: ${status}`;

    if (mode === Constants.modes.VIEWER) {
      if (status === Constants.hlsEvents.HLS_PLAYABLE) {
        const { playbackHlsUrl } = data;
        let video = document.createElement("video");
        video.setAttribute("width", "100%");
        video.setAttribute("muted", "false");
        // enableAutoPlay for browser auto play policy
        video.setAttribute("autoplay", "true");

        if (Hls.isSupported()) {
          var hls = new Hls({
            maxLoadingDelay: 4, // max video loading delay used in automatic start level selection
            defaultAudioCodec: "mp4a.40.2", // default audio codec
            startLevel: 0, // Start playback at the lowest quality level
            startPosition: -1, // set -1 playback will start from intialtime = 0
            maxBufferHole: 0.001, // 'Maximum' inter-fragment buffer hole tolerance that hls.js can cope with when searching for the next fragment to load.
            highBufferWatchdogPeriod: 0, // if media element is expected to play and if currentTime has not moved for more than highBufferWatchdogPeriod and if there are more than maxBufferHole seconds buffered upfront, hls.js will jump buffer gaps, or try to nudge playhead to recover playback.
            nudgeOffset: 0.05, // In case playback continues to stall after first playhead nudging, currentTime will be nudged evenmore following nudgeOffset to try to restore playback. media.currentTime += (nb nudge retry -1)*nudgeOffset
            nudgeMaxRetry: 3, // Max nb of nudge retries before hls.js raise a fatal BUFFER_STALLED_ERROR
            maxFragLookUpTolerance: 0.1, // This tolerance factor is used during fragment lookup.
            liveSyncDurationCount: 1, // if set to 3, playback will start from fragment N-3, N being the last fragment of the live playlist
            abrEwmaFastLive: 1, // Fast bitrate Exponential moving average half-life, used to compute average bitrate for Live streams.
            abrEwmaSlowLive: 3, // Slow bitrate Exponential moving average half-life, used to compute average bitrate for Live streams.
            abrEwmaFastVoD: 1, // Fast bitrate Exponential moving average half-life, used to compute average bitrate for VoD streams
            abrEwmaSlowVoD: 3, // Slow bitrate Exponential moving average half-life, used to compute average bitrate for VoD streams
            maxStarvationDelay: 1, // ABR algorithm will always try to choose a quality level that should avoid rebuffering
          });
          hls.loadSource(playbackHlsUrl);
          hls.attachMedia(video);
          hls.on(Hls.Events.MANIFEST_PARSED, function () {
            video.play();
          });
        } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
          video.src = playbackHlsUrl;
          video.addEventListener("canplay", function () {
            video.play();
          });
        }

        videoContainer.appendChild(video);
      }

      if (status === Constants.hlsEvents.HLS_STOPPING) {
        videoContainer.innerHTML = "";
      }
    }
  });

  if (mode === Constants.modes.CONFERENCE) {
    // creating local participant
    createLocalParticipant();

    // setting local participant stream
    meeting.localParticipant.on("stream-enabled", (stream) => {
      setTrack(stream, null, meeting.localParticipant, true);
    });

    // participant joined
    meeting.on("participant-joined", (participant) => {
      if (participant.mode === Constants.modes.CONFERENCE) {
        let videoElement = createVideoElement(
          participant.id,
          participant.displayName
        );

        participant.on("stream-enabled", (stream) => {
          setTrack(stream, audioElement, participant, false);
        });

        let audioElement = createAudioElement(participant.id);
        videoContainer.appendChild(videoElement);
        videoContainer.appendChild(audioElement);
      }
    });

    // participants left
    meeting.on("participant-left", (participant) => {
      let vElement = document.getElementById(`f-${participant.id}`);
      vElement.remove(vElement);

      let aElement = document.getElementById(`a-${participant.id}`);
      aElement.remove(aElement);
    });
  }
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

// Join Meeting As Host Button Event Listener
joinHostButton.addEventListener("click", async () => {
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  initializeMeeting(Constants.modes.CONFERENCE);
});

// Join Meeting As Viewer Button Event Listener
joinViewerButton.addEventListener("click", async () => {
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  initializeMeeting(Constants.modes.VIEWER);
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

  const { roomId } = await fetch(url, options)
    .then((response) => response.json())
    .catch((error) => alert("error", error));
  meetingId = roomId;

  initializeMeeting(Constants.modes.CONFERENCE);
});

// leave Meeting Button Event Listener
leaveButton.addEventListener("click", async () => {
  meeting?.leave();
  document.getElementById("grid-screen").style.display = "none";
  document.getElementById("join-screen").style.display = "block";
});

// Toggle Mic Button Event Listener
toggleMicButton.addEventListener("click", async () => {
  if (isMicOn) {
    // Disable Mic in Meeting
    meeting?.muteMic();
  } else {
    // Enable Mic in Meeting
    meeting?.unmuteMic();
  }
  isMicOn = !isMicOn;
});

// Toggle Web Cam Button Event Listener
toggleWebCamButton.addEventListener("click", async () => {
  if (isWebCamOn) {
    // Disable Webcam in Meeting
    meeting?.disableWebcam();

    let vElement = document.getElementById(`f-${meeting.localParticipant.id}`);
    vElement.style.display = "none";
  } else {
    // Enable Webcam in Meeting
    meeting?.enableWebcam();

    let vElement = document.getElementById(`f-${meeting.localParticipant.id}`);
    vElement.style.display = "inline";
  }
  isWebCamOn = !isWebCamOn;
});

// Start Hls Button Event Listener
startHlsButton.addEventListener("click", async () => {
  meeting?.startHls({
    layout: {
      type: "SPOTLIGHT",
      priority: "PIN",
      gridSize: 4,
    },
    theme: "LIGHT",
    mode: "video-and-audio",
    quality: "high",
    orientation: "landscape",
  });
});

// Stop Hls Button Event Listener
stopHlsButton.addEventListener("click", async () => {
  meeting?.stopHls();
});
