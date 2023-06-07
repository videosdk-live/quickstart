import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-join-screen',
  templateUrl: './join-screen.component.html',
})
export class JoinScreenComponent {
  @Output() createMeeting = new EventEmitter();
  @Output() validateMeeting = new EventEmitter<string>();
  @Input() meetingId: string = '';

  fireValidateMeeting() {
    this.validateMeeting.emit(this.meetingId);
  }

  fireCreateMeeting() {
    this.createMeeting.emit();
  }

  constructor() {}

  ngOnInit() {}
}
