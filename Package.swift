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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.6/SolanaKotlin.zip",
            checksum: "75da4a2051a80438707408d7de4577bf9bf0cd886a1afbb11bac1e262e4c9d3e"
        ),
    ]
)
