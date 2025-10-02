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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.3.5/SolanaKotlin.zip",
            checksum: "a2adf0671a1b15920a3550c538f7b01ae0cb5e1d950cc0c60cb5054eb051ae21"
        ),
    ]
)
