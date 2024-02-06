// getting Elements from Dom
const joinButton = document.getElementById("joinBtn");
const joinGuestButton = document.getElementById("joinGuestBtn");
const leaveButton = document.getElementById("leaveBtn");
const toggleMicButton = document.getElementById("toggleMicBtn");
const toggleWebCamButton = document.getElementById("toggleWebCamBtn");
const createButton = document.getElementById("createMeetingBtn");
const videoContainer = document.getElementById("videoContainer");
const textDiv = document.getElementById("textDiv");
const requestedEntriesDiv = document.getElementById("requestParticipants");

// declare Variables
let meeting = null;
let meetingId = "";
let isMicOn = false;
let isWebCamOn = false;
var isHost = null;
let requestedEntries = [];

// Join Host Meeting Button Event Listener
joinButton.addEventListener("click", async () => {
  isHost = true;
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  initializeMeeting();
});

// Join Guest Meeting Button Event Listener
joinGuestButton.addEventListener("click", async () => {
  isHost = false;
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Joining the meeting...";

  roomId = document.getElementById("meetingIdTxt").value;
  meetingId = roomId;

  initializeMeeting();
});

// Create Meeting Button Event Listener
createButton.addEventListener("click", async () => {
  isHost = true;
  document.getElementById("join-screen").style.display = "none";
  textDiv.textContent = "Please wait, we are joining the meeting";

  const url = `https://api.videosdk.live/v2/rooms`;
  const options = {
    method: "POST",
    headers: {
      Authorization: TOKEN,
      "Content-Type": "application/json",
    },
  };

  const { roomId } = await fetch(url, options)
    .then((response) => response.json())
    .catch((error) => alert("error", error));
  meetingId = roomId;

  initializeMeeting();
});

function manageRequestedEntries(entries = []) {
  const addEntry = (data) => {
    entries.push(data);
  };

  const removeEntry = (participantId) => {
    const filteredEntries = entries.filter(
      (entry) => entry.participantId !== participantId
    );
    entries = filteredEntries;
  };

  return { entries, addEntry, removeEntry };
}


// Initialize meeting
function initializeMeeting() {
  window.VideoSDK.config(isHost ? HOST_TOKEN : GUEST_TOKEN);

  meeting = window.VideoSDK.initMeeting({
    meetingId: meetingId, // required
    name: "Homi J. Bhabha", // required
    micEnabled: true, // optional, default: true
    webcamEnabled: true, // optional, default: true
  });

  meeting.join();

  // creating local participant
  createLocalParticipant();

  // setting local participant stream
  meeting.localParticipant.on("stream-enabled", (stream) => {
    setTrack(stream, null, meeting.localParticipant, true);
  });

  meeting.on("meeting-joined", () => {
    textDiv.textContent = null;

    console.log("meeting-joined");

    document.getElementById("grid-screen").style.display = "block";
    document.getElementById(
      "meetingIdHeading"
    ).textContent = `Meeting Id: ${meetingId}`;
  });

  meeting.on("meeting-left", () => {
    console.log("meeting-left");
    videoContainer.innerHTML = "";
  });

  //  participant joined
  meeting.on("participant-joined", (participant) => {
    console.log("participant-joined");
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

  // entry requested
  meeting.on("entry-requested", (data) => {
    requestedEntries.push(data);

    const entriesListElement = document.getElementById("requestedEntries");

    requestedEntries.map(({ participantId, name, allow, deny }) => {
      const entryElement = document.createElement("div");
      entryElement.setAttribute("id", `entry-${participantId}`);

      entryElement.innerHTML = `
        <p>${name} wants to join the meeting.</p>
        <button id="allowButton-${participantId}">Allow</button>
        <button id="denyButton-${participantId}" style={{ marginLeft: 8 }}>Deny</button>
      `;

      const allowButton = entryElement.querySelector(
        "#allowButton-" + participantId
      );
      allowButton.addEventListener("click", () => {
        allow(participantId);
      });

      const denyButton = entryElement.querySelector(
        "#denyButton-" + participantId
      );
      denyButton.addEventListener("click", () => {
        deny(participantId);
      });

      entriesListElement.appendChild(entryElement);
    });
  });

  // entry responded
  meeting.on("entry-responded", (participantId, decision) => {
    if (requestedEntries.length > 0) {
      const entryElement = document.getElementById(`entry-${participantId}`);

      // Remove the entry element from its parent element.
      entryElement.parentNode.removeChild(entryElement);
    }

    // participantId will be id of participant who requested to join meeting
    const filteredEntries = requestedEntries.filter(
      (entry) => entry.participantId === participantId
    );

    requestedEntries = filteredEntries;

    if (decision === "allowed") {
      // entry allowed
    } else {
      // entry denied
    }
  });

  // participants left
  meeting.on("participant-left", (participant) => {
    console.log("participant-left");
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
