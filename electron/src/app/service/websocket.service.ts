import { Injectable, signal } from '@angular/core';
import { Socket, io } from 'socket.io-client';
import { Observable, fromEvent } from 'rxjs';
import {UserInterface} from '../interface/user.interface';


@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private socket: Socket;

  // Signal pour gérer l'état de la liste des utilisateurs
  private usersSignal = signal<UserInterface[]>([]);
  public users = this.usersSignal.asReadonly();

  constructor() {
    // Initialise la connexion WebSocket avec des paramètres de reconnexion automatique
    // pour assurer une connexion robuste en cas de perte de connexion
    this.socket = io('http://localhost:9999', {
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionAttempts: 5
    });

    // Souscription aux nouveaux utilisateurs directement dans le service
    this.onNewUser().subscribe((user: UserInterface) => {
      this.usersSignal.update(users => [...users, user]);
    });
  }

  /**
   * Surveille l'état de connexion au serveur WebSocket
   * Retourne un Observable qui émet lorsque la connexion est établie
   */
  onConnect(): Observable<void> {
    return fromEvent(this.socket, 'connect') as Observable<void>;
  }

  /**
   * Surveille les déconnexions du serveur WebSocket
   * Retourne un Observable qui émet lorsque la connexion est perdue
   */
  onDisconnect(): Observable<void> {
    return fromEvent(this.socket, 'disconnect') as Observable<void>;
  }

  /**
   * Capture les erreurs de communication WebSocket
   * Retourne un Observable qui émet en cas d'erreur de connexion ou de communication
   */
  onError(): Observable<Error> {
    return fromEvent(this.socket, 'error') as Observable<Error>;
  }

  /**
   * Écoute l'arrivée de nouveaux utilisateurs
   * Retourne un Observable qui émet à chaque fois qu'un nouvel utilisateur est ajouté
   */
  onNewUser(): Observable<UserInterface> {
    return fromEvent(this.socket, 'new-user') as Observable<UserInterface>;
  }

  /**
   * Envoie les informations d'un nouvel utilisateur au serveur
   * Cette méthode est void car elle ne fait qu'émettre un événement sans attendre de réponse
   */
  sendMessage(user: UserInterface): void {
    this.socket.emit('new-user', user);
  }
}
