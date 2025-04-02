#  🤷‍♂️ Useless UI 🤷‍♂️

Useless UI is a Jetpack Compose library containing fun, but probably useless UI components. My goal is for each component module to be available as an independent Gradle dependency and also as a suite that can be imported as one dependency. This repo also contains a sample app to showcase these components.

I will be adding to this over time as I get inspiration, tinker with things, and create components I want to reuse in other side projects.

## Components

### ShatterableLayout

| ShatterableLayout | ShatterPager |
| --- | --- |
| <img src="images/shatterable_layout_demo.gif" width="300"> | <img src="images/shatterpager_demo.gif" width="300"> |

This component allows its children to be shattered into many pieces. The exact properties of this shattering are configurable via `ShatterSpec`. `ShatterableLayout` captures a bitmap of its content (the timing of this can be controlled via `CaptureMode`) and then uses that for the shattering effect. The shattering is done using a Voronoi Diagram algorithm to create non-overlapping random polygons. I was inspired by the glass shattering transition in Powerpoint, which I recall fondly adding to all of my presentations despite being completely unnecessary and annoying.

You can also do this in reverse if you want to "unshatter" something which looks neat.

#### Installation
[![](https://img.shields.io/maven-central/v/io.github.seankenkeremath/shatterable-layout)](https://search.maven.org/artifact/io.github.seankenkeremath/shatterable-layout)

```
dependencies {
    implementation("io.github.seankenkeremath:shatterable-layout:0.1.0") // Replace with latest version above
}
```

#### Optimizations
* Individual shards are rendered using cropped, smaller bitmaps to conserve memory
* Bitmap capturing can be done lazily or immediately depending on your use case
* The shattering animation of individual shards are performed at the graphics layer
* Recompositions are minimized

#### Future improvements + optimizations
* We can avoid creating cropped bitmaps entirely if Compose supports either **1)** `graphicsLayer` animations on Canvas objects (we can crop the parent bitmap in the Canvas) or **2)** cropping inside `graphicsLayer` with an arbitrary path (only simple shapes are currently supported)
* Capturing of the bitmap and creation of Voronoi cells can be offloaded to a background thread

### Wavy Components

Note: In these screenshots the "jump" is just coming from the GIF repeating. 
These components are designed to seamlessly repeat the wave pattern.

|                                                |                                                |
|------------------------------------------------|------------------------------------------------|
| <img src="images/waves_demo2.gif" width="300"> | <img src="images/waves_demo1.gif" width="300"> |

These components give you the building block to create wavy effects in your UI. 
That includes a normal `WavyLine` as well as a `WavyBox` where any combination of sides can be wavy. 
The waves are customizable via `WavySpec` which can easily be animated or controlled by the parent Composable.
`WavyBox` supports several draw styles including via `Brush` or Color.

This library also includes a `wavyPathSegment` function you can use in Path to draw a wave between 2 points for your own custom UI.

#### Installation
[![](https://img.shields.io/maven-central/v/io.github.seankenkeremath/waves)](https://search.maven.org/artifact/io.github.seankenkeremath/waves)

```
dependencies {
    implementation("io.github.seankenkeremath:waves:0.1.0") // Replace with latest version above
}
```

#### Future improvements + optimizations
* I plan to build in support for a corner radius which will make the corners for wavy box look a bit cleaner as separate wave segments converge
