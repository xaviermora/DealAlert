import { Component, signal } from '@angular/core';
import { GamesList } from '../../components/games-list/games-list';
import { GamesSearchBar } from '../../components/games-search-bar/games-search-bar';

@Component({
  selector: 'app-games-page',
  imports: [GamesList, GamesSearchBar],
  templateUrl: './games-page.html',
  styleUrl: './games-page.css',
})
export class GamesPage {
  searchGameByName = signal<string | undefined>(undefined);

  setSearchName(name: string): void {
    this.searchGameByName.set(name || undefined);
  }
}
