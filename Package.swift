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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.7/SolanaKotlin.zip",
            checksum: "7fc7a842e2139dcf9f1c8379c00f848fd546fc44527c7dd9b65193b010fef88d"
        ),
    ]
)
