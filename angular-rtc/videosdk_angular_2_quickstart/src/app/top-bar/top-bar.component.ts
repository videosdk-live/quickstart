import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.css'],
})
export class TopBarComponent {
  @Input() showTopBar: boolean = false;
  @Input() disableWebcamBtn: boolean = false;
  @Input() enableWebcamBtn: boolean = false;
  @Input() disableMicBtn: boolean = false;
  @Input() enableMicBtn: boolean = false;
  @Output() disableWebcam = new EventEmitter();
  @Output() enableWebcam = new EventEmitter();
  @Output() muteMic = new EventEmitter();
  @Output() unmuteMic = new EventEmitter();
  @Output() leaveMeeting = new EventEmitter();
  @Input() meetingId: string = '';

  constructor() {}

  fireDisableWebcam() {
    this.disableWebcam.emit();
  }

  fireEnableWebcam() {
    this.enableWebcam.emit();
  }

  fireMuteMic() {
    this.muteMic.emit();
  }

  fireUnmuteMic() {
    this.unmuteMic.emit();
  }

  fireLeaveMeeting() {
    this.leaveMeeting.emit();
  }
}
