// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v17)
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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.4.3/SolanaKotlin.zip",
            checksum: "e06c36b50687a656b1e660a5ac3df9fc5c7bc03dd222d359a3a0f9c5f0a9233d"
        ),
    ]
)
