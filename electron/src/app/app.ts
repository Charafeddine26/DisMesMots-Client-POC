import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {UserComponents} from './components/user.components';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, UserComponents],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('electron');
}
