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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.4.0/SolanaKotlin.zip",
            checksum: "f8dd82fa1624264d10c9d6192fead7363a2aa8bf66a98924332b8f6e38677309"
        ),
    ]
)
