// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "SolanaKotlin",
            targets: ["SolanaKotlin"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "SolanaKotlin",
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.5/SolanaKotlin.zip",
            checksum: "bf56d2279c0b633ae80fd68110136bd1e7ae6862e95bbc676bbe255752ff4fd9"
        ),
    ]
)
