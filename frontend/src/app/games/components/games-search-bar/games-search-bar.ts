import { Component, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-games-search-bar',
  imports: [FormsModule],
  templateUrl: './games-search-bar.html',
  styleUrl: './games-search-bar.css',
})
export class GamesSearchBar {
  newName = output<string>();

  emitNewName(value: string): void {
    this.newName.emit(value.trim());
  }
}
