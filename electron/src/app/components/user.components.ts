import {Component, computed, inject, signal, WritableSignal} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {WebsocketService} from '../service/websocket.service';
import {UserInterface} from '../interface/user.interface';
import {distinctUntilChanged, filter, map, scan} from 'rxjs';

@Component({
  selector: 'app-user',
  standalone: true,
  template: `
    <h1>User Component</h1>

    @if (isConnected()) {
      <div class="connected">Connecté au serveur</div>
    } @else {
      <div class="disconnected">Déconnecté du serveur</div>
    }

    @if (error()) {
      <div class="error">Erreur de connexion au serveur</div>
    }

    @for (user of websocketService.users(); track user.id) {
      <div class="user-card">
        <h3>{{ user.name }}</h3>
        <p>{{ user.email }}</p>
      </div>
    }

    @if (latestSignal() != null) {
      <p>Dernier Utilisateur</p>

      <div class="user-card">
        <h3>{{ latestSignal()?.name }}</h3>
        <p>{{ latestSignal()?.email }}</p>
      </div>
    }
  `
})
export class UserComponents {
  websocketService = inject(WebsocketService);
  isConnected = toSignal(this.websocketService.onConnect());
  error = toSignal(this.websocketService.onError());

  addUser(user: UserInterface) {
    this.websocketService.sendMessage(user);
  }



  // Filtrer et transformer les utilisateurs en temps réel
  latestActiveUsers$ = this.websocketService.onNewUser().pipe(
    // Ne garde que les utilisateurs actifs
    filter(user => user.status === 'active'),

    // Ajoute un timestamp à chaque utilisateur
    map(user => ({
      ...user,
      lastSeen: new Date()
    })),

    // Maintient une liste glissante des 5 derniers utilisateurs
    scan((acc, user) => {
      const users = [...acc, user];
      return users.slice(-5);
    }, [] as UserInterface[]),

    // Évite les émissions inutiles
    distinctUntilChanged()
  );

  latestSignal: WritableSignal<UserInterface|null> = signal(null);

  ngOnInit() {
    this.latestActiveUsers$.subscribe((value: UserInterface[]) => {
      if (value.length != 0) {
        this.latestSignal.set(value[0]);
      }
    });
  }
}
