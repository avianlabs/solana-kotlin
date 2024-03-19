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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.9/SolanaKotlin.zip",
            checksum: "8f9568b7b73356d3ac463a402eff921def9136839ca4f8c0e85119eacb58c2c8"
        ),
    ]
)
