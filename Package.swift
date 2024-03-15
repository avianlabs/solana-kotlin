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
            checksum: "78f9d9f25ee13e8126e71c34feb5b7e7f90a255af5b467f1c2d9b4bfbb4ced6a"
        ),
    ]
)
