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
            checksum: "12e74e10b5575ff3cb3f9ae9a6b60751c6c5972e539fef6f3a7d65bac676234c"
        ),
    ]
)
