import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-join-screen',
  templateUrl: './join-screen.component.html',
  styleUrls: ['./join-screen.component.css'],
})
export class JoinScreenComponent {
  @Output() joinMeeting = new EventEmitter();
  @Output() createMeeting = new EventEmitter();
  @Output() validateMeeting = new EventEmitter<string>();
  @Input() meetingId: string = '';

  fireJoinMeeting() {
    this.joinMeeting.emit();
  }

  fireValidateMeeting() {
    this.validateMeeting.emit(this.meetingId);
  }

  fireCreateMeeting() {
    this.createMeeting.emit();
  }

  constructor() {}

  ngOnInit() {}
}
