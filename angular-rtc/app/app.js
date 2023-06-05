var myApp = angular.module("myApp", ["config"]);

myApp.config(function () {});

myApp.run(function () {});

myApp.directive("participantGridContainer", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/participantGridContainer.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.directive("topBar", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/topBar.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.directive("joinScreen", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/joinScreen.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.directive("meetingContainer", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/meetingContainer.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.controller("myController", function ($scope, ENV) {
  // variable initialization
  $scope.name = "Homi J. Bhabha";
  $scope.meetingId = "";
  $scope.showMeetingScreen = false;
  $scope.showJoinScreen = true;
  $scope.localParticipant = null;
  $scope.participants = [];
  $scope.meeting = null;
  $scope.enableWebcamBtn = false;
  $scope.enableMicBtn = false;
  $scope.disableWebcamBtn = true;
  $scope.disableMicBtn = true;

  $scope.videoElement = document.getElementById("videoElement");
  $scope.participantGridContainer = document.getElementById(
    "participant-grid-container"
  );

  if ($scope.showJoinScreen) {
    // Check if the browser supports mediaDevices
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      // Request permission to access the camera
      navigator.mediaDevices
        .getUserMedia({ video: true })
        .then(function (stream) {
          // Set the video source to the stream from the camera
          $scope.videoElement.srcObject = stream;
        })
        .catch(function (error) {
          console.error("Error accessing camera:", error);
        });
    }
  }

  $scope.createVideoElement = function (
    stream,
    participant,
    participantMediaElement
  ) {
    var video = document.createElement("video");
    var mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    video.srcObject = mediaStream;
    video.autoplay = true;
    video.id = `v-${participant.id}`;
    video.style.marginTop = "6px";
    video.style.marginLeft = "4px";
    video.style.marginRight = "4px";
    video.style.width = "320px";
    video.style.height = "300px";
    video.style.objectFit = "cover";
    video.style.transform = "rotate('90')";
    video.style.borderRadius = "10px";
    video.setAttribute("playsinline", true);
    var videoElement = document.createElement("div");
    videoElement.setAttribute("id", `video-container-${participant.id}`);
    participantMediaElement.appendChild(videoElement);
    videoElement.appendChild(video);

    var cornerDisplayName = document.createElement("div");
    cornerDisplayName.setAttribute("id", `name-container-${participant.id}`);
    cornerDisplayName.style.position = "absolute";
    cornerDisplayName.style.bottom = "16px";
    cornerDisplayName.style.left = "16px";
    cornerDisplayName.style.color = "white";
    cornerDisplayName.style.backgroundColor = "rgba(0, 0, 0, 0.5)";
    cornerDisplayName.style.padding = "2px";
    cornerDisplayName.style.borderRadius = "2px";
    cornerDisplayName.style.fontSize = "12px";
    cornerDisplayName.style.fontWeight = "bold";
    cornerDisplayName.style.zIndex = "1";
    cornerDisplayName.style.padding = "4px";
    cornerDisplayName.innerHTML =
      participant.displayName.length > 15
        ? participant.displayName.substring(0, 15) + "..."
        : participant.displayName;
    videoElement.appendChild(cornerDisplayName);
  };

  $scope.createAudioElement = function (
    stream,
    participant,
    participantMediaElement
  ) {
    var audio = document.createElement("audio");
    var mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    audio.srcObject = mediaStream;
    audio.autoplay = true;
    audio.muted;
    audio.id = `audio-${participant.id}`;
    var audioElement = document.createElement("div");
    audioElement.setAttribute("id", `audio-container-${participant.id}`);
    participantMediaElement.appendChild(audioElement);
    audioElement.appendChild(audio);
  };

  $scope.handleStreamEnabled = function (
    stream,
    participant,
    isLocal,
    participantMediaElement
  ) {
    if (stream.kind == "video") {
      var nameElement = document.getElementById(
        `name-container-${participant.id}`
      );
      participantMediaElement.removeChild(nameElement);
      $scope.createVideoElement(stream, participant, participantMediaElement);
    }
    if (!isLocal) {
      if (stream.kind == "audio") {
        console.log("audio stream enabled");
        $scope.createAudioElement(stream, participant, participantMediaElement);
      }
    }
  };

  $scope.handleStreamDisabled = function (
    stream,
    participant,
    isLocal,
    participantMediaElement
  ) {
    if (stream.kind == "video") {
      console.log("video stream disabled");

      var videoElement = document.getElementById(
        `video-container-${participant.id}`
      );
      var nameElement = document.createElement("div");
      nameElement.setAttribute("id", `name-container-${participant.id}`);
      nameElement.innerHTML = participant.displayName.charAt(0).toUpperCase();
      nameElement.style.backgroundColor = "black";
      nameElement.style.color = "white";
      nameElement.style.textAlign = "center";
      nameElement.style.padding = "32px";
      nameElement.style.borderRadius = "100%";
      nameElement.style.fontSize = "20px";

      participantMediaElement.removeChild(videoElement);
      participantMediaElement.appendChild(nameElement);
    }
    if (!isLocal) {
      if (stream.kind == "audio") {
        console.log("audio stream disabled");
        var audioElement = document.getElementById(
          `audio-container-${participant.id}`
        );
        participantMediaElement.removeChild(audioElement);
      }
    }
  };

  $scope.joinMeeting = function () {
    window.VideoSDK.config(ENV.token); // required;

    var meeting = window.VideoSDK.initMeeting({
      meetingId: ENV.meetingId, // required
      name: $scope.name, // required
      micEnabled: true, // optional, default: true
      webcamEnabled: true, // optional, default: true
      maxResolution: "hd",
    });

    meeting.join();
    $scope.meeting = meeting;
    $scope.handleMeetingEvents(meeting);
    var showJoinScreenMessage = document.createElement("div");
    var topBar = document.getElementById("top-bar");

    topBar.style.display = "none";

    showJoinScreenMessage.setAttribute("id", "show-join-screen-message");
    showJoinScreenMessage.innerHTML = "Please wait to join meeting...";
    showJoinScreenMessage.style.color = "black";
    showJoinScreenMessage.style.fontSize = "20px";
    showJoinScreenMessage.style.fontWeight = "bold";
    showJoinScreenMessage.style.marginTop = "20px";
    showJoinScreenMessage.style.marginLeft = "20px";

    $scope.participantGridContainer.appendChild(showJoinScreenMessage);
  };

  $scope.handleMeetingEvents = function (meeting) {
    $scope.localParticipant = meeting.localParticipant;
    $scope.participants = meeting.participants;

    $scope.participantGridGenerator = function ({ participant }) {
      var participantGridItem1 = document.createElement("div");
      participantGridItem1.style.backgroundColor = "lightgrey";
      participantGridItem1.style.borderRadius = "10px";
      participantGridItem1.style.height = "300px";
      participantGridItem1.style.width = "320px";
      participantGridItem1.style.marginTop = "8px";
      participantGridItem1.style.display = "flex";
      participantGridItem1.style.alignItems = "center";
      participantGridItem1.style.justifyContent = "center";
      participantGridItem1.style.position = "relative";

      participantGridItem1.setAttribute(
        "id",
        `participant-grid-item-${participant.id}`
      );
      participantGridItem1.setAttribute("class", "col-4");
      // participantGridItem1.setAttribute("ng-controller", "myController");
      // participantGridItem1.style.display = "flex";
      var participantMediaElement1 = document.createElement("div");
      participantMediaElement1.setAttribute(
        "id",
        `participant-media-container-${participant.id}`
      );

      var nameElement = document.createElement("div");
      nameElement.setAttribute("id", `name-container-${participant.id}`);
      nameElement.innerHTML = participant.displayName.charAt(0).toUpperCase();
      nameElement.style.backgroundColor = "black";
      nameElement.style.color = "white";
      nameElement.style.textAlign = "center";
      nameElement.style.padding = "32px";
      nameElement.style.borderRadius = "100%";
      nameElement.style.fontSize = "20px";
      $scope.participantGridContainer.appendChild(participantGridItem1);
      participantGridItem1.appendChild(participantMediaElement1);

      participantMediaElement1.appendChild(nameElement);
      var participantGridItem = document.getElementById(
        `participant-grid-item-${participant.id}`
      );
      var participantMediaElement = document.getElementById(
        `participant-media-container-${participant.id}`
      );

      return {
        participantGridItem,
        participantMediaElement,
      };
    };

    meeting.on("meeting-joined", function () {
      var showJoinScreenMessage = document.getElementById(
        "show-join-screen-message"
      );
      var topBar = document.getElementById("top-bar");
      showJoinScreenMessage.style.display = "none";
      topBar.style.display = "block";

      const { participantMediaElement } = $scope.participantGridGenerator({
        participant: meeting.localParticipant,
      });

      meeting.localParticipant.on("stream-enabled", (stream) => {
        $scope.handleStreamEnabled(
          stream,
          meeting.localParticipant,
          true,
          participantMediaElement
        );
      });
      meeting.localParticipant.on("stream-disabled", (stream) => {
        $scope.handleStreamDisabled(
          stream,
          meeting.localParticipant,
          true,
          participantMediaElement
        );
      });
    });

    meeting.on("participant-left", (participant) => {
      console.log("Participant Left: ", participant.id);

      var participantGridItem = document.getElementById(
        `participant-grid-item-${participant.id}`
      );

      $scope.participantGridContainer.removeChild(participantGridItem);
    });

    meeting.on("meeting-left", () => {
      // remove all children nodes from participant grid container
      while ($scope.participantGridContainer.firstChild) {
        $scope.participantGridContainer.removeChild(
          $scope.participantGridContainer.firstChild
        );
      }
      $scope.showMeetingScreen = false;
      $scope.showJoinScreen = true;
    });

    //remote participant
    meeting.on("participant-joined", (participant) => {
      console.log("New Participant Joined: ", participant.id);

      var { participantMediaElement } = $scope.participantGridGenerator({
        participant: participant,
      });
      participant.setQuality("high");
      participant.on("stream-enabled", (stream) => {
        $scope.handleStreamEnabled(
          stream,
          participant,
          false,
          participantMediaElement
        );
      });
      participant.on("stream-disabled", (stream) => {
        $scope.handleStreamDisabled(
          stream,
          participant,
          false,
          participantMediaElement
        );
      });
    });

    if (meeting) {
      $scope.showJoinScreen = false;
      $scope.showMeetingScreen = true;
    }

    $scope.disableWebcam = function () {
      $scope.meeting.disableWebcam();
      $scope.enableWebcamBtn = true;
      $scope.disableWebcamBtn = false;
    };

    $scope.enableWebcam = function () {
      $scope.meeting.enableWebcam();
      $scope.enableWebcamBtn = false;
      $scope.disableWebcamBtn = true;
    };

    $scope.muteMic = function () {
      $scope.meeting.muteMic();
      $scope.enableMicBtn = true;
      $scope.disableMicBtn = false;
    };

    $scope.unmuteMic = function () {
      $scope.meeting.unmuteMic();
      $scope.enableMicBtn = false;
      $scope.disableMicBtn = true;
    };

    $scope.leaveMeeting = function () {
      $scope.meeting.leave();
      $scope.showMeetingScreen = false;
      $scope.showJoinScreen = true;
    };
  };
});
