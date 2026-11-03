# ExamAI Play Store assets — November 3, 2026

Updated through Google Chrome in the Brynflow Play Console account for `com.ai.studyassistant`.

## Saved in Play Console

- Default language: English (United Kingdom), en-GB.
- App title: `ExamAI: JEE NEET UPSC CAT Prep` (preserved the title edited in the console during the task).
- Short description: `short-description.txt` (78/80 characters).
- Full description: `full-description.txt` (3,098/4,000 characters).
- Eight phone images: `phone/01_welcome.png` through `phone/08_languages.png`, in that order.
- Feature graphic: `feature_graphic.png`.

The console confirmed “Your change has been saved.” Publishing overview shows five listing changes **not yet submitted for review**. Managed publishing is off; the last published app release displayed September 1, 2026. The refreshed listing describes the new local app features and should accompany the matching updated app build. This task did not create or roll out an app release, change Data safety declarations, or publish the listing.

`whats-new.txt` is prepared for the next app release but has not been attached to a release in Play Console.

## Preview and regenerate

Open `preview.html` to browse all images. Each image uses a consistent navy/teal background, a benefit-led headline and actual app screen details. The visual format follows the headline-and-framed-screen approach observed on FileForge's Play Store page. No ads or rewarded-ad offers appear in the final promotional graphics; the listing still accurately discloses that the app contains ads.

`sources/` preserves original app captures, some of which contain ad areas outside the displayed crops. These are source material only. `manifest.json` defines which screen region is shown. No quiz answers, scores or UI text were invented by image generation. Screens with progress use example study data. The language image explains that study content remains in English.

Render with macOS AppKit:

```sh
swift -module-cache-path /private/tmp/examai-swift-cache store-assets/render.swift
```

Validation: all eight phone images are opaque PNGs at 1080×1920 (9:16), approximately 1.1–1.7 MB each. The feature graphic is an opaque 1024×500 PNG, approximately 410 KB. All meet the size/dimension limits shown in Play Console, and all final images were visually inspected before upload.
