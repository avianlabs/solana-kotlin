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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.8/SolanaKotlin.zip",
            checksum: "c76c312f07deee7716d116cddc402edc445e84882b9d38d578828e25e5fe2483"
        ),
    ]
)
