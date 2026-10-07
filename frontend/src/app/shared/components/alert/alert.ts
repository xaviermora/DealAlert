import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AlertService } from './alert-service';

const DEFAULT_DURATION = 4000;
const ACTION_DURATION = 6000;
const MIN_RESUME = 1000;

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.html',
  styleUrl: './alert.css',
  host: {
    class: 'fixed inset-x-0 bottom-0 flex justify-center px-4 pb-[max(1rem,env(safe-area-inset-bottom))] sm:pb-6 pointer-events-none z-1000',
  },
})
export class Alert {
  private alertService = inject(AlertService);
  private destroyRef = inject(DestroyRef);

  alertData = signal<AlertData | null>(null);
  show = signal<boolean>(false);

  private timeoutId: ReturnType<typeof setTimeout> | undefined;
  private remaining = DEFAULT_DURATION;
  private startedAt = 0;

  constructor() {
    this.alertService.showAlert
      .pipe(takeUntilDestroyed())
      .subscribe((data) => {
        this.remaining = data.duration ?? (data.action ? ACTION_DURATION : DEFAULT_DURATION);
        this.alertData.set(data);
        this.show.set(true);
        this.startTimer();
      });

    this.destroyRef.onDestroy(() => this.clearTimer());
  }

  close() {
    this.clearTimer();
    this.show.set(false);
  }

  action() {
    const callback = this.alertData()?.action;
    if (!callback) return;

    this.close();
    callback();
  }

  // Called on hover / focus
  pause() {
    if (this.timeoutId === undefined) return;

    this.remaining -= Date.now() - this.startedAt;
    this.clearTimer();
  }

  // Called on mouse leave / blur
  resume() {
    if (!this.show() || this.timeoutId !== undefined) return;

    this.remaining = Math.max(this.remaining, MIN_RESUME);
    this.startTimer();
  }

  private startTimer() {
    this.clearTimer();
    this.startedAt = Date.now();
    this.timeoutId = setTimeout(() => {
      this.timeoutId = undefined;
      this.show.set(false);
    }, this.remaining);
  }

  private clearTimer() {
    if (this.timeoutId !== undefined) {
      clearTimeout(this.timeoutId);
      this.timeoutId = undefined;
    }
  }
}