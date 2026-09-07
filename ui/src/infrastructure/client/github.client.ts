import {Injectable} from '@angular/core';
import {map, Observable} from "rxjs";
import { HttpClient } from "@angular/common/http";

interface GitHubReleaseResponse {
  tag_name: string;
  html_url: string;
}


@Injectable({
  providedIn: 'root'
})
export class GitHubClient {
  private static url = 'https://api.github.com';

  constructor(
    private httpClient: HttpClient
  ) {
  }

  getLatestRelease(): Observable<Release> {
    return this.httpClient.get<GitHubReleaseResponse>(`${GitHubClient.url}/repos/costa-alex/workout-relay/releases/latest`).pipe(
      map(response => new Release(response)),
    )
  }
}

export class Release {
  version: string;
  url: string;

  constructor(json: GitHubReleaseResponse) {
    this.version = json.tag_name.replace(/^v/, '');
    this.url = json.html_url;
  }
}
