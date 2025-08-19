// swift-tools-version: 6.1
import PackageDescription

let package = Package(
    name: "TimeBlockiOS",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .executable(name: "TimeBlockiOS", targets: ["TimeBlockiOS"])
    ],
    dependencies: [],
    targets: [
        .executableTarget(
            name: "TimeBlockiOS",
            dependencies: [],
            path: "Sources"
        )
    ]
)
