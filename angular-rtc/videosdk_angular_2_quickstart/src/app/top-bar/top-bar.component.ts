import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.component.html',
})
export class TopBarComponent {
  @Input() showTopBar: boolean = false;
  @Output() toogleWebcam = new EventEmitter();
  @Output() toogleMic = new EventEmitter();
  @Output() toggleScreenShare = new EventEmitter();
  @Output() leaveMeeting = new EventEmitter();
  @Input() meetingId: string = '';

  constructor() {}

  fireToggleWebcam() {
    this.toogleWebcam.emit();
  }

  fireToggleMic() {
    this.toogleMic.emit();
  }

  fireToggleScreenShare() {
    this.toggleScreenShare.emit();
  }

  fireLeaveMeeting() {
    this.leaveMeeting.emit();
  }
}
