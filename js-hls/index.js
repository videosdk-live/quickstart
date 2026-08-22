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

function showJoinScreen(message) {
  document.getElementById("join-screen").style.display = "block";
  document.getElementById("grid-screen").style.display = "none";
  textDiv.textContent = message ?? "";
}

// Initialize meeting
async function initializeMeeting(mode) {
  try {
    // VideoSDK.config and initMeeting are synchronous in 1.x — no await.
    window.VideoSDK.config(TOKEN);

    meeting = window.VideoSDK.initMeeting({
      meetingId: meetingId, // required
      name: "Thomas Edison", // required
      mode: mode,
    });

    // Register all event handlers before joining so no event is missed.
    meeting.on("meeting-joined", async () => {
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

      if (mode === Constants.modes.SEND_AND_RECV) {
        document.getElementById("speakerView").style.display = "block";

        // Pin the local participant if he joins in `SEND_AND_RECV` mode
        try {
          await meeting.localParticipant.pin();
        } catch (error) {
          console.error("Failed to pin the local participant", error);
        }
      }
    });

    meeting.on("meeting-left", () => {
      videoContainer.innerHTML = "";
    });

    meeting.on("hls-state-changed", (data) => {
      const { status } = data;

      hlsStatusHeading.textContent = `HLS Status: ${status}`;

      if (mode === Constants.modes.SIGNALLING_ONLY) {
        if (status === Constants.hlsEvents.HLS_PLAYABLE) {
          const { playbackHlsUrl } = data;
          let video = document.createElement("video");
          video.setAttribute("width", "100%");
          video.setAttribute("muted", "false");
          // enableAutoPlay for browser auto play policy
          video.setAttribute("autoplay", "true");

          if (Hls.isSupported()) {
            var hls = new Hls({
              maxLoadingDelay: 1, // max video loading delay used in automatic start level selection
              defaultAudioCodec: "mp4a.40.2", // default audio codec
              maxBufferLength: 0, // If buffer length is/become less than this value, a new fragment will be loaded
              maxMaxBufferLength: 1, // Hls.js will never exceed this value
              startLevel: 0, // Start playback at the lowest quality level
              startPosition: -1, // set -1 playback will start from intialtime = 0
              maxBufferHole: 0.001, // 'Maximum' inter-fragment buffer hole tolerance that hls.js can cope with when searching for the next fragment to load.
              highBufferWatchdogPeriod: 0, // if media element is expected to play and if currentTime has not moved for more than highBufferWatchdogPeriod and if there are more than maxBufferHole seconds buffered upfront, hls.js will jump buffer gaps, or try to nudge playhead to recover playback.
              nudgeOffset: 0.05, // In case playback continues to stall after first playhead nudging, currentTime will be nudged evenmore following nudgeOffset to try to restore playback. media.currentTime += (nb nudge retry -1)*nudgeOffset
              nudgeMaxRetry: 1, // Max nb of nudge retries before hls.js raise a fatal BUFFER_STALLED_ERROR
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

    if (mode === Constants.modes.SEND_AND_RECV) {
      // creating local participant
      createLocalParticipant();

      // setting local participant stream
      meeting.localParticipant.on("stream-enabled", (stream) => {
        setTrack(stream, null, meeting.localParticipant, true);
      });

      // participant joined
      meeting.on("participant-joined", async (participant) => {
        if (participant.mode === Constants.modes.SEND_AND_RECV) {
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

          try {
            await participant.pin();
          } catch (error) {
            console.error("Failed to pin the participant", error);
          }
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

    // `join()` resolves when the join request is accepted — wait for the
    // `meeting-joined` event before calling other meeting methods.
    await meeting.join();
  } catch (error) {
    console.error("Failed to initialize meeting", error);
    videoContainer.innerHTML = "";
    showJoinScreen("Unable to join the meeting. Please try again.");
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

  await initializeMeeting(Constants.modes.SEND_AND_RECV);
});

// Join Meeting As Viewer Button Event Listener
joinViewerButton.addEventListener("click", async () => {
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  await initializeMeeting(Constants.modes.SIGNALLING_ONLY);
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
    await initializeMeeting(Constants.modes.SEND_AND_RECV);
  } catch (error) {
    console.error("Failed to create meeting", error);
    showJoinScreen(
      "Unable to create the meeting. Check your token and try again."
    );
  }
});

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

// Start Hls Button Event Listener
startHlsButton.addEventListener("click", async () => {
  try {
    await meeting?.startHls({
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
  } catch (error) {
    console.error("Failed to start HLS", error);
  }
});

// Stop Hls Button Event Listener
stopHlsButton.addEventListener("click", async () => {
  try {
    await meeting?.stopHls();
  } catch (error) {
    console.error("Failed to stop HLS", error);
  }
});
