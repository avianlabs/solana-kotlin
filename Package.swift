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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.10/SolanaKotlin.zip",
            checksum: "7fbe9798d3f3562a7f8f7f59f37b23df3ac250ab261bd491d18253dcf0f87c86"
        ),
    ]
)
