import { Component, ElementRef, Renderer2, ViewChild } from '@angular/core';
import { VideoSDK } from '@videosdk.live/js-sdk';
import { environment } from './../enviroments/enviroment';
import { MeetingService } from './meeting.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent {
  @ViewChild('participantGridContainer') participantGridContainer: ElementRef;
  @ViewChild('participantScreenShareContainer')
  participantScreenShareContainer: ElementRef;

  title = 'videosdk_angular_2_quickstart';
  meeting: any;
  participantName: string = '';
  meetingId: string = '';
  showJoinScreen: boolean = true;
  showMeetingScreen: boolean = false;
  showTopBar: boolean = false;
  localParticipant: any;
  participants: any[] = [];
  isWebcamOn: boolean = true;
  isMicOn: boolean = true;
  isScreenShareOn: boolean = true;

  participant: any;

  constructor(
    private renderer: Renderer2,
    private meetingService: MeetingService
  ) {
    this.participantGridContainer = new ElementRef(null);
    this.participantScreenShareContainer = new ElementRef(null);
    this.participantName = 'Homi J. Bhabha';
  }

  createMeeting() {
    this.meetingService.createMeeting().subscribe(
      (roomId) => {
        this.meetingId = roomId;
        this.joinMeeting();
      },
      (error) => {
        console.error('Failed to create meeting:', error);
      }
    );
  }

  validateMeeting(meetingId: any) {
    this.meetingService.validateMeeting(meetingId).subscribe(
      (isValid) => {
        if (isValid) {
          this.meetingId = meetingId;
          this.joinMeeting();
        } else {
          alert('Invalid meeting id');
        }
      },
      (error) => {
        console.error('Failed to validate meeting:', error);
        // Handle the error
      }
    );
  }

  async initMeeting() {
    VideoSDK.config(environment.token);

    this.meeting = VideoSDK.initMeeting({
      meetingId: this.meetingId, // required
      name: this.participantName, // required
      micEnabled: true, // optional, default: true
      webcamEnabled: true, // optional, default: true
      maxResolution: 'hd', // optional, default: "hd"
    });
  }

  joinMeeting() {
    this.initMeeting();
    this.meeting.join();

    this.handleMeetingEvents(this.meeting);
    const showJoinScreenMessage = this.renderer.createElement('div');

    this.renderer.setAttribute(
      showJoinScreenMessage,
      'id',
      'show-join-screen-message'
    );
    this.renderer.setProperty(
      showJoinScreenMessage,
      'innerHTML',
      'Please wait to join meeting...'
    );
    this.renderer.setStyle(showJoinScreenMessage, 'color', 'black');
    this.renderer.setStyle(showJoinScreenMessage, 'fontSize', '20px');
    this.renderer.setStyle(showJoinScreenMessage, 'fontWeight', 'bold');
    this.renderer.setStyle(showJoinScreenMessage, 'marginTop', '20px');
    this.renderer.setStyle(showJoinScreenMessage, 'marginLeft', '20px');
    this.renderer.appendChild(document.body, showJoinScreenMessage);
  }

  createVideoElement(
    stream: any,
    participant: any,
    participantMediaElement: any
  ) {
    const video = this.renderer.createElement('video');
    const mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    this.renderer.setAttribute(video, 'id', `v-${participant.id}`);
    this.renderer.setAttribute(video, 'autoplay', 'true');
    this.renderer.setAttribute(video, 'playsinline', 'true');
    this.renderer.setAttribute(video, 'muted', 'true');
    this.renderer.setAttribute(
      video,
      'style',
      'width: 100%; height: 100%;position: absolute;top: 0;left: 0;object-fit: cover;'
    );
    this.renderer.setProperty(video, 'srcObject', mediaStream);
    const videoElement = this.renderer.createElement('div');
    this.renderer.setAttribute(
      videoElement,
      'id',
      `video-container-${participant.id}`
    );

    this.renderer.setAttribute(
      videoElement,
      'style',
      'width: 100%; height: 100%;'
    );
    this.renderer.setStyle(videoElement, 'position', 'relative');

    this.renderer.appendChild(participantMediaElement, videoElement);
    this.renderer.appendChild(videoElement, video);

    const cornerDisplayName = this.renderer.createElement('div');
    this.renderer.setAttribute(
      cornerDisplayName,
      'id',
      `name-container-${participant.id}`
    );
    this.renderer.setStyle(cornerDisplayName, 'position', 'absolute');
    this.renderer.setStyle(cornerDisplayName, 'bottom', '16px');
    this.renderer.setStyle(cornerDisplayName, 'left', '16px');
    this.renderer.setStyle(cornerDisplayName, 'color', 'white');
    this.renderer.setStyle(
      cornerDisplayName,
      'backgroundColor',
      'rgba(0, 0, 0, 0.5)'
    );
    this.renderer.setStyle(cornerDisplayName, 'padding', '2px');
    this.renderer.setStyle(cornerDisplayName, 'borderRadius', '2px');
    this.renderer.setStyle(cornerDisplayName, 'fontSize', '12px');
    this.renderer.setStyle(cornerDisplayName, 'fontWeight', 'bold');
    this.renderer.setStyle(cornerDisplayName, 'zIndex', '1');
    this.renderer.setStyle(cornerDisplayName, 'padding', '4px');
    cornerDisplayName.innerHTML =
      participant.displayName.length > 15
        ? participant.displayName.substring(0, 15) + '...'
        : participant.displayName;
    this.renderer.appendChild(videoElement, cornerDisplayName);
  }

  createAudioElement(
    stream: any,
    participant: any,
    participantMediaElement: any
  ) {
    const audio = this.renderer.createElement('audio');
    const mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    this.renderer.setAttribute(audio, 'id', `audio-${participant.id}`);
    this.renderer.setAttribute(audio, 'autoplay', 'true');
    this.renderer.setAttribute(audio, 'playsinline', 'true');
    this.renderer.setAttribute(audio, 'muted', 'true');
    this.renderer.setProperty(audio, 'srcObject', mediaStream);

    const audioElement = this.renderer.createElement('div');
    this.renderer.setAttribute(
      audioElement,
      'id',
      `audio-container-${participant.id}`
    );
    this.renderer.appendChild(participantMediaElement, audioElement);
    this.renderer.appendChild(audioElement, audio);
  }

  createNameElemeent(participant: any) {
    var nameElement = this.renderer.createElement('div');
    this.renderer.setAttribute(
      nameElement,
      'id',
      `name-container-${participant.id}`
    );
    nameElement.innerHTML = participant.displayName.charAt(0).toUpperCase();
    this.renderer.setStyle(nameElement, 'backgroundColor', 'black');
    this.renderer.setStyle(nameElement, 'color', 'white');
    this.renderer.setStyle(nameElement, 'textAlign', 'center');
    this.renderer.setStyle(nameElement, 'padding', '32px');
    this.renderer.setStyle(nameElement, 'borderRadius', '100%');
    this.renderer.setStyle(nameElement, 'fontSize', '20px');
    return nameElement;
  }

  createShareAudioElement(stream: any, participant: any) {
    if (participant.pId == this.meeting.localParticipant.id) return;
    const audio = this.renderer.createElement('audio');
    const mediaStream = new MediaStream();

    this.renderer.setAttribute(audio, 'id', `a-share-${participant.id}`);
    this.renderer.setAttribute(audio, 'autoplay', 'true');
    this.renderer.setAttribute(audio, 'playsinline', 'true');
    this.renderer.setAttribute(audio, 'muted', 'true');
    this.renderer.setProperty(audio, 'srcObject', mediaStream);
    this.renderer.appendChild(
      this.participantScreenShareContainer.nativeElement,
      audio
    );
  }

  createShareVideoElement(stream: any, participant: any) {
    if (participant.id == this.meeting.localParticipant.id) return;

    const video = this.renderer.createElement('video');
    const mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    this.renderer.setAttribute(video, 'id', `v-share-${participant.id}`);
    this.renderer.setAttribute(video, 'autoplay', 'true');
    this.renderer.setAttribute(video, 'controls', 'false');

    this.renderer.setAttribute(
      video,
      'style',
      'width: 100%; height: 100%;object-fit: cover;background-color: red;'
    );
    this.renderer.setProperty(video, 'srcObject', mediaStream);

    this.renderer.appendChild(
      this.participantScreenShareContainer.nativeElement,
      video
    );
  }

  handleStreamEnabled(
    stream: any,
    participant: any,
    isLocal: any,
    participantMediaElement: any
  ) {
    if (stream.kind == 'video') {
      var nameElement = document.getElementById(
        `name-container-${participant.id}`
      );
      participantMediaElement.removeChild(nameElement);
      this.createVideoElement(stream, participant, participantMediaElement);
    }
    if (!isLocal) {
      if (stream.kind == 'audio') {
        this.createAudioElement(stream, participant, participantMediaElement);
      }
    }
  }

  handleScreenShareStreamEnabled(stream: any, participant: any, isLocal: any) {
    if (stream.kind == 'share') {
      this.createShareVideoElement(stream, participant);
    }
    if (!isLocal) {
      if (stream.kind == 'audio') {
        console.log('audio stream enabled');
        this.createShareAudioElement(stream, participant);
      }
    }
  }

  handleStreamDisabled(
    stream: any,
    participant: any,
    isLocal: any,
    participantMediaElement: any
  ) {
    if (stream.kind == 'video') {
      console.log('video stream disabled');

      var videoElement = document.getElementById(
        `video-container-${participant.id}`
      );

      var nameElement = this.createNameElemeent(participant);
      this.renderer.removeChild(participantMediaElement, videoElement);
      this.renderer.appendChild(participantMediaElement, nameElement);
    }
    if (!isLocal) {
      if (stream.kind == 'audio') {
        var audioElement = document.getElementById(
          `audio-container-${participant.id}`
        );
        this.renderer.removeChild(participantMediaElement, audioElement);
      }
    }
  }

  handleScreenShareStreamDisabled(stream: any, participant: any, isLocal: any) {
    if (stream.kind == 'share') {
      var videoElement = document.getElementById(`v-share-${participant.id}`);
      this.renderer.removeChild(
        this.participantScreenShareContainer.nativeElement,
        videoElement
      );
    }
    if (!isLocal) {
      if (stream.kind == 'audio') {
        var audioElement = document.getElementById(`a-share-${participant.id}`);
        this.renderer.removeChild(
          this.participantScreenShareContainer.nativeElement,
          audioElement
        );
      }
    }
  }

  participantGridGenerator(participant: any) {
    var participantGridItem = this.renderer.createElement('div');
    this.renderer.setStyle(participantGridItem, 'backgroundColor', 'lightgrey');
    this.renderer.setStyle(participantGridItem, 'borderRadius', '10px');
    this.renderer.setStyle(participantGridItem, 'aspectRatio', 16 / 9);
    this.renderer.setStyle(participantGridItem, 'width', '360px');
    this.renderer.setStyle(participantGridItem, 'marginTop', '8px');
    this.renderer.setStyle(participantGridItem, 'display', 'flex');
    this.renderer.setStyle(participantGridItem, 'alignItems', 'center');
    this.renderer.setStyle(participantGridItem, 'justifyContent', 'center');
    this.renderer.setStyle(participantGridItem, 'overflow', 'hidden');

    this.renderer.setAttribute(
      participantGridItem,
      'id',
      `participant-grid-item-${participant.id}`
    );

    this.renderer.setAttribute(participantGridItem, 'class', 'col-4');

    var participantMediaElement = this.renderer.createElement('div');
    this.renderer.setAttribute(
      participantMediaElement,
      'id',
      `participant-media-container-${participant.id}`
    );
    this.renderer.setStyle(participantMediaElement, 'position', 'relative');
    this.renderer.setStyle(participantMediaElement, 'width', '100%');
    this.renderer.setStyle(participantMediaElement, 'height', '100%');
    this.renderer.setStyle(participantMediaElement, 'display', 'flex');
    this.renderer.setStyle(participantMediaElement, 'alignItems', 'center');
    this.renderer.setStyle(participantMediaElement, 'justifyContent', 'center');
    var nameElement = this.createNameElemeent(participant);
    this.renderer.appendChild(
      this.participantGridContainer.nativeElement,
      participantGridItem
    );

    this.renderer.appendChild(participantGridItem, participantMediaElement);
    this.renderer.appendChild(participantMediaElement, nameElement);
    this.renderer.setStyle(this.participantScreenShareContainer.nativeElement, 'display', 'block');


    var getParticipantMediaElement = document.getElementById(
      `participant-media-container-${participant.id}`
    );

    return {
      getParticipantMediaElement,
    };
  }

  handleMeetingEvents(meeting: any) {
    this.localParticipant = meeting.localParticipant;
    this.participants = meeting.participants;

    meeting.on('meeting-joined', () => {
      var showJoinScreenMessage = document.getElementById(
        'show-join-screen-message'
      );
      this.renderer.removeChild(document.body, showJoinScreenMessage);
      const { getParticipantMediaElement } = this.participantGridGenerator(
        this.meeting.localParticipant
      );
      this.showTopBar = true;

      meeting.localParticipant.on('stream-enabled', (stream: any) => {
        console.log('Stream Enabled: ');
        this.handleStreamEnabled(
          stream,
          meeting.localParticipant,
          true,
          getParticipantMediaElement
        );
      });
      meeting.localParticipant.on('stream-disabled', (stream: any) => {
        console.log('Stream Disabled: ');
        this.handleStreamDisabled(
          stream,
          meeting.localParticipant,
          true,
          getParticipantMediaElement
        );
        if (stream.kind === 'share') {
          this.isScreenShareOn = !this.isScreenShareOn;
        }
      });
    });

    meeting.on('participant-left', (participant: any) => {
      console.log('Participant Left: ', participant.id);

      var participantGridItem = document.getElementById(
        `participant-grid-item-${participant.id}`
      );
      this.participantGridContainer.nativeElement.removeChild(
        participantGridItem
      );
    });

    meeting.on('meeting-left', () => {
      console.log('Meeting Left');
      // remove all children nodes from participant grid container
      while (this.participantGridContainer.nativeElement.firstChild) {
        this.participantGridContainer.nativeElement.removeChild(
          this.participantGridContainer.nativeElement.firstChild
        );
      }
      this.showMeetingScreen = false;
      this.showJoinScreen = true;
    });

    //remote participant
    meeting.on('participant-joined', (participant: any) => {
      console.log('New Participant Joined: ', participant.id);

      var { getParticipantMediaElement } =
        this.participantGridGenerator(participant);
      participant.setQuality('high');
      participant.on('stream-enabled', (stream: any) => {
        this.handleStreamEnabled(
          stream,
          participant,
          false,
          getParticipantMediaElement
        );
        if (stream.kind === 'share') {
          this.handleScreenShareStreamEnabled(stream, participant, false);
        }
      });
      participant.on('stream-disabled', (stream: any) => {
        this.handleStreamDisabled(
          stream,
          participant,
          false,
          getParticipantMediaElement
        );
        if (stream.kind === 'share') {
          this.handleScreenShareStreamDisabled(stream, participant, false);
          this.isScreenShareOn = !this.isScreenShareOn;
        }
      });
    });

    if (meeting) {
      this.showJoinScreen = false;
      this.showMeetingScreen = true;
    }
  }

  toogleWebcam() {
    if (this.isWebcamOn) {
      this.meeting.disableWebcam();
    } else {
      this.meeting.enableWebcam();
    }
    this.isWebcamOn = !this.isWebcamOn;
  }

  toogleMic() {
    if (this.isMicOn) {
      this.meeting.muteMic();
    } else {
      this.meeting.unmuteMic();
    }
    this.isMicOn = !this.isMicOn;
  }

  toggleScreenShare() {
    if (this.isScreenShareOn) {
      this.meeting.enableScreenShare();
    } else {
      this.meeting.disableScreenShare();
    }
    this.isScreenShareOn = !this.isScreenShareOn;
  }

  leaveMeeting() {
    this.meeting.leave();
    this.showMeetingScreen = false;
    this.showJoinScreen = true;
  }
}
