import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JoinScreenComponent } from './join-screen/join-screen.component';
import { TopBarComponent } from './top-bar/top-bar.component';
import { MeetingService } from './meeting.service';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [AppComponent, JoinScreenComponent, TopBarComponent],
  imports: [BrowserModule, AppRoutingModule, HttpClientModule, FormsModule],
  providers: [MeetingService],
  bootstrap: [AppComponent],
})
export class AppModule {}
