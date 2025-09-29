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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.3.4/SolanaKotlin.zip",
            checksum: "5e2675c96dabd20456a0d83937484b51b65232e2459c3e8883c6ef909cd592c2"
        ),
    ]
)
