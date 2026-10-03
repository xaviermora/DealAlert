import { Component, DestroyRef, ElementRef, effect, inject, input, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, debounceTime, distinctUntilChanged, exhaustMap, finalize, startWith, switchMap, tap } from 'rxjs';
import { GameService } from '../../services/game-service';
import { GameCard } from '../game-card/game-card';

@Component({
  selector: 'app-games-list',
  imports: [GameCard],
  templateUrl: './games-list.html',
  styleUrl: './games-list.css',
})
export class GamesList {
  private readonly pageSize = 20;
  private readonly gameService = inject(GameService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly loadNextPage$ = new Subject<void>();

  name = input.required<string | undefined>();
  loadMoreTrigger = viewChild<ElementRef<HTMLElement>>('loadMoreTrigger');

  games = signal<Game[]>([]);
  loading = signal(true);
  hasMore = signal(true);
  loadError = signal(false);

  constructor() {
    toObservable(this.name).pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap((name) => {
        this.games.set([]);
        this.loading.set(false);
        this.hasMore.set(true);
        this.loadError.set(false);
        let nextPage = 0;

        return this.loadNextPage$.pipe(
          startWith(undefined),
          exhaustMap(() => {
            if (this.loading() || !this.hasMore()) return EMPTY;

            this.loading.set(true);
            this.loadError.set(false);

            return this.gameService.getGames(this.pageSize, nextPage, name).pipe(
              tap((response) => {
                const page = response.data;
                if (!page) {
                  this.hasMore.set(false);
                  return;
                }

                this.games.update((games) => [...games, ...page.content]);
                nextPage += 1;
                this.hasMore.set(!page.last && page.content.length > 0);
              }),
              catchError(() => {
                this.loadError.set(true);
                return EMPTY;
              }),
              finalize(() => this.loading.set(false))
            );
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();

    effect((onCleanup) => {
      const trigger = this.loadMoreTrigger()?.nativeElement;
      if (!trigger || typeof IntersectionObserver === 'undefined') return;

      const observer = new IntersectionObserver((entries) => {
        if (entries.some((entry) => entry.isIntersecting)) this.loadNextPage$.next();
      }, { rootMargin: '400px 0px' });

      observer.observe(trigger);
      onCleanup(() => observer.disconnect());
    });
  }

  retryLoading(): void {
    this.loadNextPage$.next();
  }
}
