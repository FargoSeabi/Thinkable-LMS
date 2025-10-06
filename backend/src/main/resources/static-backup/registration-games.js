// Registration Assessment Games
// These games help determine learning preferences without direct medical questions

let gameScores = {
    focusGame: 0,
    memoryGame: 0,
    reactionGame: 0,
    executiveGame: 0,
    sensoryGame: 0
};

let gamesCompleted = 0;
let gameState = {};

// Utility functions for analysis
function calculateVariance(numbers) {
    if (numbers.length === 0) return 0;
    const mean = numbers.reduce((a, b) => a + b) / numbers.length;
    const variance = numbers.reduce((sum, num) => sum + Math.pow(num - mean, 2), 0) / numbers.length;
    return variance;
}

function calculateStandardDeviation(numbers) {
    return Math.sqrt(calculateVariance(numbers));
}

// Enhanced Focus Game - Tests sustained attention, impulse control, and consistency
function startFocusGame() {
    const canvas = document.getElementById('focusCanvas');
    const ctx = canvas.getContext('2d');
    const startBtn = document.querySelector('#focusGame .game-btn');
    
    startBtn.disabled = true;
    startBtn.textContent = 'Playing...';

    let score = 100;
    let gameTime = 30000; // Extended to 30 seconds for better assessment
    let startTime = Date.now();
    let targetX = canvas.width / 2;
    let targetY = canvas.height / 2;
    let playerX = targetX;
    let playerY = targetY;
    
    // Track performance metrics for neurodivergent detection
    let performanceData = {
        samples: [],
        lapses: 0, // Attention lapses (out of target for >2 seconds)
        variability: [],
        impulsiveMovements: 0
    };
    
    gameState.focusActive = true;
    gameState.focusData = performanceData;

    // Draw initial state
    function drawGame() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Draw target area (center)
        ctx.strokeStyle = '#2ecc71';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(targetX, targetY, 30, 0, 2 * Math.PI);
        ctx.stroke();

        // Draw player dot
        let distance = Math.sqrt(Math.pow(playerX - targetX, 2) + Math.pow(playerY - targetY, 2));
        let inTarget = distance <= 30;
        
        ctx.fillStyle = inTarget ? '#2ecc71' : '#e74c3c';
        ctx.beginPath();
        ctx.arc(playerX, playerY, 8, 0, 2 * Math.PI);
        ctx.fill();

        // Record performance data for analysis
        let currentTime = Date.now() - startTime;
        performanceData.samples.push({
            time: currentTime,
            distance: distance,
            inTarget: inTarget,
            x: playerX,
            y: playerY
        });

        // Calculate score based on distance from center
        if (distance > 30) {
            score = Math.max(0, score - 0.5); // Lose points when outside target
        }

        // Track attention lapses and variability for ADHD detection
        if (performanceData.samples.length > 10) {
            let recentSamples = performanceData.samples.slice(-10);
            let outOfTargetCount = recentSamples.filter(s => !s.inTarget).length;
            
            if (outOfTargetCount >= 8) { // 80% out of target in last second
                performanceData.lapses++;
            }
            
            // Track movement variability (high variability = potential ADHD indicator)
            let movements = recentSamples.map(s => Math.sqrt(s.x * s.x + s.y * s.y));
            let variance = calculateVariance(movements);
            performanceData.variability.push(variance);
        }
    }

    // Add random movement to make it challenging
    function addMovement() {
        if (!gameState.focusActive) return;
        
        // Slight random drift to test sustained attention
        playerX += (Math.random() - 0.5) * 2;
        playerY += (Math.random() - 0.5) * 2;
        
        // Keep within canvas bounds
        playerX = Math.max(10, Math.min(canvas.width - 10, playerX));
        playerY = Math.max(10, Math.min(canvas.height - 10, playerY));
    }

    // Mouse control
    canvas.addEventListener('mousemove', (e) => {
        if (!gameState.focusActive) return;
        const rect = canvas.getBoundingClientRect();
        playerX = e.clientX - rect.left;
        playerY = e.clientY - rect.top;
    });

    let gameLoop = setInterval(() => {
        if (!gameState.focusActive) {
            clearInterval(gameLoop);
            return;
        }

        addMovement();
        drawGame();

        // Check if time is up
        if (Date.now() - startTime > gameTime) {
            gameState.focusActive = false;
            
            // Calculate comprehensive score based on multiple metrics
            let finalScore = calculateFocusScore(performanceData, score);
            gameScores.focusGame = finalScore;
            
            document.getElementById('focusScore').textContent = gameScores.focusGame + '%';
            startBtn.textContent = 'Completed!';
            startBtn.style.backgroundColor = '#2ecc71';
            completeGame();
            clearInterval(gameLoop);
        }
    }, 50);

    // Initial draw
    drawGame();
}

// Calculate comprehensive focus score for neurodivergent detection
function calculateFocusScore(performanceData, basicScore) {
    // Base score from game performance
    let score = Math.max(0, Math.min(100, basicScore));
    
    // Analyze consistency (key ADHD indicator)
    if (performanceData.samples.length > 50) {
        let distances = performanceData.samples.map(s => s.distance);
        let variability = calculateStandardDeviation(distances);
        
        // High variability suggests attention difficulties
        if (variability > 20) score -= 15; // ADHD pattern
        if (variability > 30) score -= 10; // Severe attention issues
        
        // Store detailed metrics for backend analysis
        gameState.focusMetrics = {
            attentionLapses: performanceData.lapses,
            consistencyScore: Math.max(0, 100 - variability * 2),
            averageDistance: distances.reduce((a, b) => a + b) / distances.length,
            timeInTarget: performanceData.samples.filter(s => s.inTarget).length / performanceData.samples.length * 100
        };
    }
    
    return Math.round(score);
}

// Executive Function Game - Tests task switching and cognitive flexibility
function startExecutiveGame() {
    const grid = document.getElementById('executiveGrid');
    const instruction = document.getElementById('taskInstruction');
    const startBtn = document.querySelector('#executiveGame .game-btn');
    const items = grid.querySelectorAll('.exec-item');
    
    startBtn.disabled = true;
    startBtn.textContent = 'Playing...';

    let score = 100;
    let round = 0;
    let maxRounds = 15;
    let currentTask = '';
    let currentTarget = '';
    let switchCount = 0;
    let errors = 0;
    let reactionTimes = [];
    let taskSwitchPenalties = [];
    
    const tasks = [
        { type: 'shape', instruction: 'Click the CIRCLE', target: 'circle' },
        { type: 'shape', instruction: 'Click the SQUARE', target: 'square' },
        { type: 'number', instruction: 'Click the number 1', target: '1' },
        { type: 'number', instruction: 'Click the number 2', target: '2' },
        { type: 'color', instruction: 'Click the RED circle', target: 'red' },
        { type: 'color', instruction: 'Click the BLUE circle', target: 'blue' }
    ];

    function nextRound() {
        if (round >= maxRounds) {
            // Calculate final score based on task switching performance
            let switchingScore = calculateExecutiveScore(reactionTimes, taskSwitchPenalties, errors, switchCount);
            gameScores.executiveGame = switchingScore;
            document.getElementById('executiveScore').textContent = gameScores.executiveGame + '%';
            startBtn.textContent = 'Completed!';
            startBtn.style.backgroundColor = '#2ecc71';
            completeGame();
            return;
        }

        round++;
        
        // Deliberately switch tasks to test cognitive flexibility
        let lastTask = currentTask;
        currentTask = tasks[Math.floor(Math.random() * tasks.length)];
        
        // Track task switches (important for ADHD assessment)
        if (lastTask && lastTask.type !== currentTask.type) {
            switchCount++;
        }

        instruction.textContent = currentTask.instruction;
        currentTarget = currentTask.target;
        
        // Start reaction time measurement
        window.executiveStartTime = Date.now();
        
        // Reset item styles
        items.forEach(item => {
            item.style.backgroundColor = '';
            item.style.transform = '';
        });
    }

    // Add click handlers
    items.forEach(item => {
        item.addEventListener('click', function() {
            if (round === 0 || round > maxRounds) return;
            
            let reactionTime = Date.now() - window.executiveStartTime;
            let isCorrect = item.dataset.value === currentTarget;
            
            reactionTimes.push(reactionTime);
            
            if (isCorrect) {
                score += 2;
                item.style.backgroundColor = '#2ecc71';
                item.style.transform = 'scale(1.2)';
                
                // Measure task switch penalty (slower after switches = ADHD indicator)
                if (switchCount > 0) {
                    taskSwitchPenalties.push(reactionTime);
                }
                
                setTimeout(nextRound, 1000);
            } else {
                errors++;
                score = Math.max(0, score - 5);
                item.style.backgroundColor = '#e74c3c';
                item.style.transform = 'scale(0.8)';
                setTimeout(() => {
                    item.style.backgroundColor = '';
                    item.style.transform = '';
                }, 500);
            }
        });
    });

    // Start first round
    setTimeout(nextRound, 1000);
}

function calculateExecutiveScore(reactionTimes, switchPenalties, errors, switches) {
    let baseScore = Math.max(0, 100 - errors * 5);
    
    // Analyze task switching efficiency (key executive function measure)
    if (switchPenalties.length > 0 && reactionTimes.length > 5) {
        let averageNormal = reactionTimes.slice(0, -switchPenalties.length).reduce((a, b) => a + b, 0) / (reactionTimes.length - switchPenalties.length);
        let averageSwitch = switchPenalties.reduce((a, b) => a + b, 0) / switchPenalties.length;
        
        let switchCost = averageSwitch - averageNormal;
        
        // High switch cost indicates executive function difficulties (ADHD)
        if (switchCost > 500) baseScore -= 20; // Significant EF deficit
        if (switchCost > 300) baseScore -= 10; // Mild EF challenges
        
        // Store metrics for detailed analysis
        gameState.executiveMetrics = {
            taskSwitchCost: switchCost,
            errorRate: errors / 15 * 100,
            averageReactionTime: reactionTimes.reduce((a, b) => a + b, 0) / reactionTimes.length,
            consistencyScore: 100 - calculateStandardDeviation(reactionTimes) / 10
        };
    }
    
    return Math.round(Math.max(0, baseScore));
}

// Sensory Processing Game - Tests filtering and sensory sensitivity
function startSensoryGame() {
    const display = document.getElementById('sensoryDisplay');
    const target = document.getElementById('sensoryTarget');
    const distractionZone = document.getElementById('distractionZone');
    const startBtn = document.querySelector('#sensoryGame .game-btn');
    
    startBtn.disabled = true;
    startBtn.textContent = 'Playing...';

    let score = 100;
    let round = 0;
    let maxRounds = 12;
    let distractionLevel = 1;
    let performanceWithDistraction = [];
    let baselinePerformance = [];
    let currentRoundStart = 0;
    
    function createDistractions(level) {
        distractionZone.innerHTML = '';
        
        // Add visual distractions
        for (let i = 0; i < level * 3; i++) {
            const distraction = document.createElement('div');
            distraction.className = 'visual-distraction';
            distraction.style.cssText = `
                position: absolute;
                width: 20px;
                height: 20px;
                background: ${['red', 'blue', 'green', 'yellow'][Math.floor(Math.random() * 4)]};
                border-radius: 50%;
                left: ${Math.random() * 80}%;
                top: ${Math.random() * 80}%;
                animation: bounce 0.5s infinite alternate;
            `;
            distractionZone.appendChild(distraction);
        }
        
        // Add movement to target (sensory challenge)
        if (level > 2) {
            target.style.animation = 'wiggle 0.3s infinite';
        }
    }

    function nextRound() {
        if (round >= maxRounds) {
            let sensoryScore = calculateSensoryScore(performanceWithDistraction, baselinePerformance);
            gameScores.sensoryGame = sensoryScore;
            document.getElementById('sensoryScore').textContent = gameScores.sensoryGame + '%';
            startBtn.textContent = 'Completed!';
            startBtn.style.backgroundColor = '#2ecc71';
            completeGame();
            return;
        }

        round++;
        currentRoundStart = Date.now();
        
        // Gradually increase distraction level
        distractionLevel = Math.min(5, Math.floor(round / 3) + 1);
        
        if (round <= 3) {
            // Baseline rounds with minimal distraction
            createDistractions(0);
        } else {
            // Test with increasing sensory load
            createDistractions(distractionLevel);
        }
        
        // Position target randomly
        target.style.left = Math.random() * 80 + '%';
        target.style.top = Math.random() * 80 + '%';
    }

    // Click handler for target
    target.addEventListener('click', function() {
        if (round === 0) return;
        
        let reactionTime = Date.now() - currentRoundStart;
        
        if (round <= 3) {
            baselinePerformance.push(reactionTime);
        } else {
            performanceWithDistraction.push({
                time: reactionTime,
                distractionLevel: distractionLevel,
                round: round
            });
        }
        
        score += 3;
        target.style.transform = 'scale(1.3)';
        setTimeout(() => {
            target.style.transform = 'scale(1)';
            nextRound();
        }, 500);
    });

    // Start first round
    setTimeout(nextRound, 1000);
}

function calculateSensoryScore(withDistraction, baseline) {
    if (baseline.length === 0 || withDistraction.length === 0) return 50;
    
    let baselineAvg = baseline.reduce((a, b) => a + b, 0) / baseline.length;
    let distractedAvg = withDistraction.reduce((sum, item) => sum + item.time, 0) / withDistraction.length;
    
    // Calculate performance degradation under sensory load
    let degradation = (distractedAvg - baselineAvg) / baselineAvg * 100;
    
    // Score based on ability to filter distractions
    let score = Math.max(0, 100 - degradation);
    
    // Store metrics for sensory processing analysis
    gameState.sensoryMetrics = {
        baselineReactionTime: baselineAvg,
        distractedReactionTime: distractedAvg,
        performanceDegradation: degradation,
        sensoryOverloadIndicator: degradation > 50 // Potential sensory processing differences
    };
    
    return Math.round(score);
}

// Memory Game - Tests working memory and visual processing
function startMemoryGame() {
    const grid = document.getElementById('memoryGrid');
    const cells = grid.querySelectorAll('.memory-cell');
    const startBtn = document.querySelector('#memoryGame .game-btn');
    
    startBtn.disabled = true;
    startBtn.textContent = 'Watch carefully...';

    let sequence = [];
    let playerSequence = [];
    let level = 1;
    let score = 0;
    let isPlaying = false;

    function generateSequence() {
        sequence = [];
        for (let i = 0; i < level + 2; i++) {
            sequence.push(Math.floor(Math.random() * 4));
        }
    }

    function playSequence() {
        isPlaying = true;
        let index = 0;
        
        const playNext = () => {
            if (index >= sequence.length) {
                isPlaying = false;
                startBtn.textContent = 'Your turn - repeat the sequence!';
                return;
            }

            const cellIndex = sequence[index];
            const cell = cells[cellIndex];
            
            // Highlight the cell
            cell.style.backgroundColor = cell.dataset.color;
            cell.style.transform = 'scale(1.1)';
            
            setTimeout(() => {
                cell.style.backgroundColor = '#f0f0f0';
                cell.style.transform = 'scale(1)';
                index++;
                setTimeout(playNext, 500);
            }, 600);
        };
        
        playNext();
    }

    function checkSequence() {
        if (playerSequence.length === sequence.length) {
            let correct = true;
            for (let i = 0; i < sequence.length; i++) {
                if (playerSequence[i] !== sequence[i]) {
                    correct = false;
                    break;
                }
            }

            if (correct) {
                score += level * 20;
                level++;
                if (level <= 3) {
                    playerSequence = [];
                    startBtn.textContent = `Level ${level} - Watch...`;
                    setTimeout(() => {
                        generateSequence();
                        playSequence();
                    }, 1500);
                } else {
                    // Game completed successfully
                    gameScores.memoryGame = Math.min(100, score);
                    document.getElementById('memoryScore').textContent = gameScores.memoryGame + '%';
                    startBtn.textContent = 'Completed!';
                    startBtn.style.backgroundColor = '#2ecc71';
                    completeGame();
                }
            } else {
                // Wrong sequence
                gameScores.memoryGame = Math.max(0, score - 10);
                document.getElementById('memoryScore').textContent = gameScores.memoryGame + '%';
                startBtn.textContent = 'Completed!';
                startBtn.style.backgroundColor = '#2ecc71';
                completeGame();
            }
        }
    }

    // Add click handlers to cells
    cells.forEach((cell, index) => {
        cell.addEventListener('click', () => {
            if (isPlaying) return;
            
            playerSequence.push(index);
            
            // Visual feedback
            cell.style.backgroundColor = cell.dataset.color;
            setTimeout(() => {
                cell.style.backgroundColor = '#f0f0f0';
            }, 300);
            
            checkSequence();
        });
    });

    // Start the game
    generateSequence();
    setTimeout(() => {
        playSequence();
    }, 1000);
}

// Reaction Time Game - Tests processing speed and attention
function startReactionGame() {
    const target = document.getElementById('reactionTarget');
    const startBtn = document.querySelector('#reactionGame .game-btn');
    
    startBtn.disabled = true;
    startBtn.textContent = 'Get ready...';

    let reactionTimes = [];
    let round = 0;
    let maxRounds = 5;
    let waiting = false;
    let startTime = 0;

    function nextRound() {
        if (round >= maxRounds) {
            // Calculate average reaction time and score
            let avgReactionTime = reactionTimes.reduce((a, b) => a + b, 0) / reactionTimes.length;
            let score = Math.max(0, Math.min(100, 100 - (avgReactionTime - 200) / 10));
            
            gameScores.reactionGame = Math.round(score);
            document.getElementById('reactionScore').textContent = gameScores.reactionGame + '%';
            startBtn.textContent = 'Completed!';
            startBtn.style.backgroundColor = '#2ecc71';
            target.style.backgroundColor = '#f0f0f0';
            target.textContent = `Average: ${Math.round(avgReactionTime)}ms`;
            completeGame();
            return;
        }

        round++;
        target.style.backgroundColor = '#e74c3c';
        target.textContent = `Round ${round}/${maxRounds} - Wait...`;
        waiting = true;

        // Random delay between 1-4 seconds
        let delay = 1000 + Math.random() * 3000;
        
        setTimeout(() => {
            if (!waiting) return; // User clicked too early
            
            target.style.backgroundColor = '#2ecc71';
            target.textContent = 'CLICK NOW!';
            startTime = Date.now();
            waiting = false;
        }, delay);
    }

    target.addEventListener('click', () => {
        if (waiting) {
            // Clicked too early
            target.textContent = 'Too early! Wait for green...';
            return;
        }
        
        if (startTime > 0) {
            let reactionTime = Date.now() - startTime;
            reactionTimes.push(reactionTime);
            startTime = 0;
            
            target.textContent = `${reactionTime}ms - Nice!`;
            setTimeout(nextRound, 1500);
        }
    });

    // Start first round
    setTimeout(nextRound, 1000);
}

function completeGame() {
    gamesCompleted++;
    
    // Require all 5 games to be completed for comprehensive assessment
    if (gamesCompleted === 5) {
        // All games completed
        document.getElementById('gamesCompletion').style.display = 'block';
        
        // Enable next button
        document.querySelector('.next-btn').disabled = false;
        
        // Add celebration animation
        const celebration = document.querySelector('.completion-message i');
        celebration.style.animation = 'bounce 1s infinite';
        
        // Store comprehensive assessment data
        gameState.comprehensiveAssessment = {
            focusMetrics: gameState.focusMetrics || {},
            executiveMetrics: gameState.executiveMetrics || {},
            sensoryMetrics: gameState.sensoryMetrics || {},
            timestamp: Date.now(),
            totalGamesCompleted: gamesCompleted
        };
    }
}

// Initialize games when step 2 is shown
function initializeGames() {
    // Reset game states
    gamesCompleted = 0;
    gameScores = { focusGame: 0, memoryGame: 0, reactionGame: 0 };
    
    // Reset UI
    document.querySelectorAll('.game-btn').forEach(btn => {
        btn.disabled = false;
        btn.textContent = 'Start Game';
        btn.style.backgroundColor = '';
    });
    
    document.querySelectorAll('.game-score span').forEach(span => {
        span.textContent = '-';
    });
    
    document.getElementById('gamesCompletion').style.display = 'none';
}

// Export game scores for registration
function getGameScores() {
    return gameScores;
}