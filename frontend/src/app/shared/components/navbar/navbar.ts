import { Component, ElementRef, HostListener, computed, inject, signal, viewChild } from '@angular/core';
import { NavigationEnd, Router, RouterEvent, RouterLink, RouterLinkActive } from "@angular/router";
import { AuthService } from '../../../auth/services/auth-service';
import { ApiErrorCode } from '../../models/ApiErrorCode';
import { AlertService } from '../alert/alert-service';
import { TelegramLink } from '../../../auth/components/telegram-link/telegram-link';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, TelegramLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);
  private accountMenuRef = viewChild<ElementRef<HTMLElement>>('accountMenuRef');
  activeMenu = signal<boolean>(false);
  activeAccountMenu = signal<boolean>(false);
  showTelegramLink = signal<boolean>(false);

  isAuthenticated = this.authService.isAuthenticated;
  currentAccount = this.authService.currentAccount;
  isTelegramLinked = computed(() => this.currentAccount()?.telegramUserId != null);
  initial = computed(() => this.currentAccount()?.email?.charAt(0).toUpperCase() ?? '?');

  navigateHomePage = () => {
    this.router.navigateByUrl('');
    this.activeMenu.set(false)
    this.activeAccountMenu.set(false);
  }

  toggleMenu(){
    this.activeMenu.update(value => !value);
    this.activeAccountMenu.set(false);
  }

  toggleAccountMenu() {
    this.activeAccountMenu.update(value => !value);
  }

  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: MouseEvent) {
    if (!this.activeAccountMenu()) return;

    const container = this.accountMenuRef()?.nativeElement;
    if (container && !container.contains(event.target as Node)) {
      this.activeAccountMenu.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  handleEscape() {
    this.activeAccountMenu.set(false);
  }

  openTelegramLink() {
    this.showTelegramLink.set(true);
    this.activeAccountMenu.set(false);
  }

  authAction(){
    let auth = this.isAuthenticated()

    if(auth){
      this.authService.logout().subscribe({
        next: () => {
          this.router.navigateByUrl('/games');
          this.alertService.newAlert({
            type: 'success',
            text: 'Sesión cerrada.'
          })
        }, 
        error: (err: ApiResponse<undefined>) => {
          if(err.error === ApiErrorCode.INTERNAL_SERVER_ERROR){
            this.alertService.newAlert({
              type: 'error',
              text: 'Error cerrando sesión.'
            })
          }
        }
      })
    }else{
      this.router.navigateByUrl('/login')
    }

    this.activeMenu.set(false)
    this.activeAccountMenu.set(false);
  }

  telegramUnlinked() {
    this.activeAccountMenu.set(false);
    this.authService.unlinkTelegram().subscribe({
      next: () => {
        this.alertService.newAlert({
          type: 'success',
          text: 'Se desvinculó telegram.'
        })
      }, error: () => {
        this.alertService.newAlert({
          type: 'error',
          text: 'Error inesperado.'
        })
      }
    });
  }
}
